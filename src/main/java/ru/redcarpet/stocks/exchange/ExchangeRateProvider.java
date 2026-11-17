package ru.redcarpet.stocks.exchange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public interface ExchangeRateProvider {

    /** Курс currency → baseCurrency на дату (за 1 единицу currency). */
    Optional<BigDecimal> getRate(Currency currency, Currency baseCurrency, LocalDate date);

    /**
     * Курсы currency → baseCurrency за период (включительно).
     * Возвращаются только дни, за которые провайдер знает курс.
     * По умолчанию — по одному запросу на день; провайдеры с пакетным API переопределяют.
     */
    default Map<LocalDate, BigDecimal> getRates(Currency currency,
                                                Currency baseCurrency,
                                                LocalDate from,
                                                LocalDate to) {
        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            Optional<BigDecimal> rate = getRate(currency, baseCurrency, date);
            if (rate.isPresent()) {
                result.put(date, rate.get());
            }
        }
        return result;
    }
}
