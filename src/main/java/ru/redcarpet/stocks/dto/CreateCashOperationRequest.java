package ru.redcarpet.stocks.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.redcarpet.stocks.enums.CashOperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

public record CreateCashOperationRequest(
        @NotNull Long portfolioId,
        @NotNull CashOperationType operationType,
        @NotNull LocalDate operationDate,
        @NotNull Currency currency,
        @NotNull @Positive BigDecimal amount,
        String comment
) {
}
