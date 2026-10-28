package ru.redcarpet.stocks.model.dto;

import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

public record CreateCashOperationCommand(
        Long portfolioId,
        CashOperationType operationType,
        CashFlowDirection direction,
        LocalDate operationDate,
        Currency currency,
        BigDecimal amount,
        String platform,
        TradeSource source,
        String comment,
        DividendFields dividend
) {
    public record DividendFields(
            String issuer,
            String isin,
            LocalDate recordDate,
            String tax
    ) {}
}