package ru.redcarpet.stocks.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

public record CreateCurrencyExchangeRequest(
        @NotNull Long portfolioId,
        @NotNull LocalDate operationDate,
        @NotNull Currency sourceCurrency,
        @NotNull @Positive BigDecimal sourceAmount,
        @NotNull Currency targetCurrency,
        @NotNull @Positive BigDecimal targetAmount,
        @Positive BigDecimal exchangeRate,
        String comment
) { }
