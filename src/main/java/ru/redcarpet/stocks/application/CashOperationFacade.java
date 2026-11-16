package ru.redcarpet.stocks.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.CreateCashOperationCommand;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CashOperationService;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;

/**
 * Оркестратор ручных кассовых операций: операция и движение денег
 * выполняются в одной транзакции.
 */
@Service
@RequiredArgsConstructor
public class CashOperationFacade {

    private final CashOperationService cashOperationService;
    private final BalanceService balanceService;

    @Transactional
    public CashOperation createManual(CreateCashOperationCommand command) {
        if (command.source() != null && command.source() != TradeSource.MANUAL) {
            throw new ValidationException(
                    "Only MANUAL cash operations can be created via API");
        }

        CashOperation operation = cashOperationService.create(command);

        BigDecimal delta = operation.getDirection() == CashFlowDirection.INCOME
                ? operation.getAmount()
                : operation.getAmount().negate();
        balanceService.apply(
                operation.getPortfolio().getId(),
                operation.getCurrency(),
                delta);

        return operation;
    }
}
