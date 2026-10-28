package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.CreateCashOperationCommand;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

public interface CashOperationService {
    CashOperation create(CreateCashOperationCommand command);

    long count(Long portfolioId, TradeSource source,
               LocalDate date, CashOperationType type,
               String platform, CashFlowDirection direction,
               BigDecimal amount, Currency currency);
}
