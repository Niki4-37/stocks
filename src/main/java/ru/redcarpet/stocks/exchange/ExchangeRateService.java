package ru.redcarpet.stocks.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.entity.ExchangeRateEntity;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.repositories.ExchangeRateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateProvider provider;
    private final ExchangeRateRepository repository;

    @Transactional
    public BigDecimal getRate(Currency from, Currency to, LocalDate date) {
        if (from.equals(to)) return BigDecimal.ONE;

        // 1) кэш на точную дату
        Optional<BigDecimal> cached = findRate(from, to, date);
        if (cached.isPresent()) return cached.get();

        // 2) провайдер (ЦБ)
        RuntimeException providerFailure = null;
        try {
            Optional<BigDecimal> provided = provider.getRate(from, to, date);
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

    private void saveRate(Currency from, Currency to, LocalDate date, BigDecimal rate) {
        ExchangeRateEntity entity = new ExchangeRateEntity();
        entity.setRateDate(date);
        entity.setCurrency(from);
        entity.setBaseCurrency(to);
        entity.setRate(rate);
        entity.setSource("CBR");
        repository.save(entity);
    }
}
