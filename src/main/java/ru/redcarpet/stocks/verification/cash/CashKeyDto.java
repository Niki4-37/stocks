package ru.redcarpet.stocks.verification.cash;

import jakarta.validation.constraints.NotNull;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

public record CashKeyDto(
        @NotNull LocalDate operationDate,
        @NotNull CashOperationType operationType,
        String platform,
        @NotNull CashFlowDirection direction,
        @NotNull BigDecimal amount,
        Currency currency
) {
}
