package ru.redcarpet.stocks.exchange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

public interface ExchangeRateProvider {

    /** Курс currency → baseCurrency на дату (за 1 единицу currency). */
    Optional<BigDecimal> getRate(Currency currency, Currency baseCurrency, LocalDate date);
}