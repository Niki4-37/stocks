package ru.redcarpet.stocks.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.redcarpet.stocks.model.dto.CreateCashOperationCommand;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CashOperationService;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashOperationFacadeTest {

    private static final Currency RUB = Currency.getInstance("RUB");

    @Mock
    private CashOperationService cashOperationService;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private CashOperationFacade facade;

    @Test
    void creditCashOnIncome() {
        CreateCashOperationCommand command = command(TradeSource.MANUAL, CashFlowDirection.INCOME, "500.00");
        when(cashOperationService.create(command))
                .thenReturn(operation(CashFlowDirection.INCOME, "500.00"));

        facade.createManual(command);

        assertApplied("500.00");
    }

    @Test
    void debitCashOnExpense() {
        CreateCashOperationCommand command = command(TradeSource.MANUAL, CashFlowDirection.EXPENSE, "500.00");
        when(cashOperationService.create(command))
                .thenReturn(operation(CashFlowDirection.EXPENSE, "500.00"));

        facade.createManual(command);

        assertApplied("-500.00");
    }

    @Test
    void rejectNonManualSource() {
        CreateCashOperationCommand command =
                command(TradeSource.BROKER_REPORT, CashFlowDirection.INCOME, "500.00");

        assertThatThrownBy(() -> facade.createManual(command))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(cashOperationService, balanceService);
    }

    @Test
    void propagateBalanceFailure() {
        CreateCashOperationCommand command = command(TradeSource.MANUAL, CashFlowDirection.EXPENSE, "500.00");
        when(cashOperationService.create(command))
                .thenReturn(operation(CashFlowDirection.EXPENSE, "500.00"));
        doThrow(new ValidationException("insufficient funds"))
                .when(balanceService).apply(eq(1L), eq(RUB), any());

        assertThatThrownBy(() -> facade.createManual(command))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("insufficient funds");
    }

    private void assertApplied(String expected) {
        ArgumentCaptor<BigDecimal> amount = ArgumentCaptor.forClass(BigDecimal.class);
        verify(balanceService).apply(eq(1L), eq(RUB), amount.capture());
        assertThat(amount.getValue()).isEqualByComparingTo(expected);
    }

    private static CreateCashOperationCommand command(TradeSource source,
                                                      CashFlowDirection direction,
                                                      String amount) {
        return new CreateCashOperationCommand(
                1L,
                CashOperationType.DEPOSIT,
                direction,
                LocalDate.of(2026, 1, 15),
                RUB,
                new BigDecimal(amount),
                null,
                source,
                "test",
                null);
    }

    private static CashOperation operation(CashFlowDirection direction, String amount) {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        CashOperation operation = new CashOperation();
        operation.setPortfolio(portfolio);
        operation.setCurrency(RUB);
        operation.setDirection(direction);
        operation.setAmount(new BigDecimal(amount));
        operation.setSource(TradeSource.MANUAL);
        return operation;
    }
}
