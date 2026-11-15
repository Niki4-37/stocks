package ru.redcarpet.stocks.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.redcarpet.stocks.model.entity.ExchangeRateEntity;

import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {

    Optional<ExchangeRateEntity> findByRateDateAndCurrencyAndBaseCurrency(
            LocalDate rateDate, Currency currency, Currency baseCurrency);

    Optional<ExchangeRateEntity> findFirstByCurrencyAndBaseCurrencyAndRateDateLessThanEqualOrderByRateDateDesc(
            Currency currency, Currency baseCurrency, LocalDate rateDate);
}