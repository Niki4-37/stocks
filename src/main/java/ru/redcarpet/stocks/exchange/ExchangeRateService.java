package ru.redcarpet.stocks.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import ru.redcarpet.stocks.model.entity.ExchangeRateEntity;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.repositories.ExchangeRateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    /** Запас до начала периода: выходные/праздники перед первой датой истории. */
    private static final int LEADING_DAYS = 14;

    /** После 429 не дёргаем ЦБ, чтобы не продлевать блокировку DDoS-Guard. */
    private static final Duration PROVIDER_COOLDOWN = Duration.ofSeconds(60);

    private final ExchangeRateProvider provider;
    private final ExchangeRateRepository repository;

    /** Момент, до которого обращения к провайдеру пропускаются (после 429). */
    private volatile Instant providerCooldownUntil = Instant.EPOCH;

    @Transactional
    public BigDecimal getRate(Currency from, Currency to, LocalDate date) {
        if (from.equals(to)) return BigDecimal.ONE;

        // 1) кэш на точную дату
        Optional<BigDecimal> cached = findRate(from, to, date);
        if (cached.isPresent()) return cached.get();

        // 2) провайдер (ЦБ)
        RuntimeException providerFailure = null;
        try {
            Optional<BigDecimal> provided = callProviderOnce(from, to, date);
            if (provided.isPresent()) {
                saveRate(from, to, date, provided.get());
                return provided.get();
            }
        } catch (RuntimeException e) {
            providerFailure = e;
            log.warn("Exchange rate provider failed for {}→{} on {}",
                    from, to, date, e);
        }

        // 3) последний известный курс — выходные/праздники или сбой ЦБ
        Optional<BigDecimal> fallback = repository
                .findFirstByCurrencyAndBaseCurrencyAndRateDateLessThanEqualOrderByRateDateDesc(
                        from, to, date)
                .map(ExchangeRateEntity::getRate);
        if (fallback.isPresent()) {
            log.warn("Using earlier exchange rate {}→{} for date {}", from, to, date);
            return fallback.get();
        }

        if (providerFailure != null) throw providerFailure;
        throw new NotFoundException(
                "Exchange rate not found: " + from + "→" + to + " on " + date);
    }

    /**
     * Курсы нескольких валют к базовой за период, пригодные для построения дневной истории.
     * <p>
     * Кэш из БД читается одним запросом; у провайдера добираются только краевые пропуски
     * (начало/конец периода), а не каждый день. Это убирает сотни HTTP-запросов к ЦБ.
     *
     * @return валюта → (дата → курс к base), отсортировано по дате
     */
    @Transactional
    public Map<Currency, NavigableMap<LocalDate, BigDecimal>> ratesToBase(
            Collection<Currency> currencies,
            Currency base,
            LocalDate from,
            LocalDate to) {

        Map<Currency, NavigableMap<LocalDate, BigDecimal>> result = new HashMap<>();
        if (from == null || to == null || from.isAfter(to)) return result;

        Set<Currency> needed = new LinkedHashSet<>();
        for (Currency currency : currencies) {
            if (currency == null || currency.equals(base)) continue;
            needed.add(currency);
        }
        if (needed.isEmpty()) return result;

        LocalDate lookupFrom = from.minusDays(LEADING_DAYS);

        // 1) весь кэш диапазона — одним запросом
        for (ExchangeRateEntity entity : repository.findByBaseCurrencyAndCurrencyInAndRateDateBetween(
                base, needed, lookupFrom, to)) {
            result.computeIfAbsent(entity.getCurrency(), c -> new TreeMap<>())
                    .put(entity.getRateDate(), entity.getRate());
        }

        // 2) добор у провайдера только по краям периода
        List<ExchangeRateEntity> fetched = new ArrayList<>();
        for (Currency currency : needed) {
            NavigableMap<LocalDate, BigDecimal> byDate =
                    result.computeIfAbsent(currency, c -> new TreeMap<>());

            for (LocalDate[] range : missingRanges(byDate, lookupFrom, to)) {
                Map<LocalDate, BigDecimal> loaded = loadFromProvider(currency, base, range[0], range[1]);
                loaded.forEach((date, rate) -> {
                    byDate.put(date, rate);
                    fetched.add(newRateEntity(currency, base, date, rate));
                });
            }
        }
        if (!fetched.isEmpty()) {
            repository.saveAll(fetched);
        }

        // 3) без курса хотя бы одной валюты историю посчитать нельзя
        for (Currency currency : needed) {
            NavigableMap<LocalDate, BigDecimal> byDate = result.get(currency);
            if (byDate == null || byDate.isEmpty()) {
                throw new NotFoundException(
                        "Exchange rate not found: " + currency + "→" + base
                                + " for " + lookupFrom + ".." + to);
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<BigDecimal> findRate(Currency from, Currency to, LocalDate date) {
        if (from.equals(to)) return Optional.of(BigDecimal.ONE);
        return repository.findByRateDateAndCurrencyAndBaseCurrency(date, from, to)
                .map(ExchangeRateEntity::getRate);
    }

    /** Конвертирует сумму из `from` в `to` по курсу на дату. */
    @Transactional
    public BigDecimal convert(BigDecimal amount, Currency from, Currency to, LocalDate date) {
        if (amount == null || amount.signum() == 0) return BigDecimal.ZERO;
        if (from.equals(to)) return amount;

        BigDecimal rate = getRate(from, to, date);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    // ---- helpers ----

    /**
     * Участки периода, которых нет в кэше: только начало до первого известного курса
     * и конец после последнего. Внутренние «дыры» — это выходные и праздники.
     */
    private static List<LocalDate[]> missingRanges(NavigableMap<LocalDate, BigDecimal> byDate,
                                                   LocalDate from,
                                                   LocalDate to) {
        List<LocalDate[]> ranges = new ArrayList<>(2);

        if (byDate.isEmpty()) {
            ranges.add(new LocalDate[]{from, to});
            return ranges;
        }

        LocalDate first = byDate.firstKey();
        if (first.isAfter(from)) {
            ranges.add(new LocalDate[]{from, first.minusDays(1)});
        }

        LocalDate last = byDate.lastKey();
        if (last.isBefore(to)) {
            ranges.add(new LocalDate[]{last.plusDays(1), to});
        }

        return ranges;
    }

    private Map<LocalDate, BigDecimal> loadFromProvider(Currency currency,
                                                        Currency base,
                                                        LocalDate from,
                                                        LocalDate to) {
        if (isProviderCoolingDown()) {
            log.warn("Skip exchange rate provider for {}→{} ({}..{}): cooldown until {}",
                    currency, base, from, to, providerCooldownUntil);
            return Map.of();
        }

        try {
            Map<LocalDate, BigDecimal> loaded = provider.getRates(currency, base, from, to);
            return loaded == null ? Map.of() : loaded;
        } catch (RuntimeException e) {
            if (isTooManyRequests(e)) {
                providerCooldownUntil = Instant.now().plus(PROVIDER_COOLDOWN);
            }
            log.warn("Exchange rate provider failed for {}→{} ({}..{})",
                    currency, base, from, to, e);
            return Map.of();
        }
    }

    private Optional<BigDecimal> callProviderOnce(Currency from, Currency to, LocalDate date) {
        if (isProviderCoolingDown()) {
            log.warn("Skip exchange rate provider for {}→{} on {}: cooldown until {}",
                    from, to, date, providerCooldownUntil);
            return Optional.empty();
        }
        try {
            return provider.getRate(from, to, date);
        } catch (RuntimeException e) {
            if (isTooManyRequests(e)) {
                providerCooldownUntil = Instant.now().plus(PROVIDER_COOLDOWN);
            }
            throw e;
        }
    }

    private boolean isProviderCoolingDown() {
        return Instant.now().isBefore(providerCooldownUntil);
    }

    private static boolean isTooManyRequests(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof RestClientResponseException response
                    && response.getStatusCode().value() == 429) {
                return true;
            }
        }
        return false;
    }

    private void saveRate(Currency from, Currency to, LocalDate date, BigDecimal rate) {
        repository.save(newRateEntity(from, to, date, rate));
    }

    private static ExchangeRateEntity newRateEntity(Currency from,
                                                    Currency to,
                                                    LocalDate date,
                                                    BigDecimal rate) {
        ExchangeRateEntity entity = new ExchangeRateEntity();
        entity.setRateDate(date);
        entity.setCurrency(from);
        entity.setBaseCurrency(to);
        entity.setRate(rate);
        entity.setSource("CBR");
        return entity;
    }
}
