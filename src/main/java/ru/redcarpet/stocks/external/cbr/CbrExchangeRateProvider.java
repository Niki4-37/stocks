package ru.redcarpet.stocks.external.cbr;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.exchange.ExchangeRateProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CbrExchangeRateProvider implements ExchangeRateProvider {

    private static final Currency RUB = Currency.getInstance("RUB");

    private final CbrParsedClient client;

    @Override
    public Optional<BigDecimal> getRate(Currency currency, Currency baseCurrency, LocalDate date) {
        // XML_daily.asp отдаёт курс и за нерабочие дни (последний опубликованный), поэтому
        // для точечных запросов он удобнее dynamic-выборки.
        if (currency.equals(baseCurrency)) return Optional.of(BigDecimal.ONE);

        Map<Currency, BigDecimal> ratesToRub = client.getRatesOnDate(date);

        BigDecimal fromRub = ratesToRub.get(currency);
        BigDecimal toRub = ratesToRub.get(baseCurrency);

        if (fromRub == null || toRub == null) return Optional.empty();
        return Optional.of(fromRub.divide(toRub, 8, RoundingMode.HALF_UP));
    }

    @Override
    public Map<LocalDate, BigDecimal> getRates(Currency currency,
                                               Currency baseCurrency,
                                               LocalDate from,
                                               LocalDate to) {
        // ЦБ даёт RUB → валюта; для base ≠ RUB — кросс через RUB
        if (currency.equals(baseCurrency)) return Map.of();

        Map<LocalDate, BigDecimal> currencyToRub = currency.equals(RUB)
                ? Map.of()
                : client.getRatesForRange(currency, from, to);

        Map<LocalDate, BigDecimal> baseToRub = baseCurrency.equals(RUB)
                ? Map.of()
                : client.getRatesForRange(baseCurrency, from, to);

        Set<LocalDate> dates = new LinkedHashSet<>(currencyToRub.keySet());
        dates.addAll(baseToRub.keySet());

        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>(dates.size());
        for (LocalDate date : dates) {
            BigDecimal fromRub = currency.equals(RUB) ? BigDecimal.ONE : currencyToRub.get(date);
            BigDecimal toRub = baseCurrency.equals(RUB) ? BigDecimal.ONE : baseToRub.get(date);

            if (fromRub == null || toRub == null) continue;
            result.put(date, fromRub.divide(toRub, 8, RoundingMode.HALF_UP));
        }
        return result;
    }
}
