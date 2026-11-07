package ru.redcarpet.stocks.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

public record CreateCashOperationRequest(
        @NotNull Long portfolioId,
        @NotNull CashOperationType operationType,
        @NotNull CashFlowDirection direction,
        @NotNull LocalDate operationDate,
        @NotNull Currency currency,
        @NotNull @Positive BigDecimal amount,
        String platform,
        String comment
) {
}