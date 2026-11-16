package ru.redcarpet.stocks.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.redcarpet.stocks.model.dto.CreateCurrencyExchangeRequest;
import ru.redcarpet.stocks.model.entity.CurrencyTradeEntity;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.enums.CurrencyTradeOperation;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CurrencyService;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyFacadeTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency RUB = Currency.getInstance("RUB");

    @Mock
    private CurrencyService currencyService;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private CurrencyFacade facade;

    @Test
    void buyDebitsCounterAndCreditsLot() {
        CreateCurrencyExchangeRequest request = request(CurrencyTradeOperation.BUY, TradeSource.MANUAL);
        when(currencyService.exchange(request)).thenReturn(trade(CurrencyTradeOperation.BUY));

        facade.exchangeManual(request);

        InOrder inOrder = inOrder(balanceService);
        inOrder.verify(balanceService).debit(eq(1L), eq(RUB), eq(new BigDecimal("9000.00")));
        inOrder.verify(balanceService).credit(eq(1L), eq(USD), eq(new BigDecimal("100.00")));
    }

    @Test
    void sellDebitsLotAndCreditsCounter() {
        CreateCurrencyExchangeRequest request = request(CurrencyTradeOperation.SELL, TradeSource.MANUAL);
        when(currencyService.exchange(request)).thenReturn(trade(CurrencyTradeOperation.SELL));

        facade.exchangeManual(request);

        InOrder inOrder = inOrder(balanceService);
        inOrder.verify(balanceService).debit(eq(1L), eq(USD), eq(new BigDecimal("100.00")));
        inOrder.verify(balanceService).credit(eq(1L), eq(RUB), eq(new BigDecimal("9000.00")));
    }

    @Test
    void rejectNonManualSource() {
        CreateCurrencyExchangeRequest request =
                request(CurrencyTradeOperation.BUY, TradeSource.BROKER_REPORT);

        assertThatThrownBy(() -> facade.exchangeManual(request))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(currencyService, balanceService);
    }

    @Test
    void propagateDebitFailure() {
        CreateCurrencyExchangeRequest request = request(CurrencyTradeOperation.BUY, TradeSource.MANUAL);
        when(currencyService.exchange(request)).thenReturn(trade(CurrencyTradeOperation.BUY));
        doThrow(new ValidationException("insufficient funds"))
                .when(balanceService).debit(eq(1L), eq(RUB), any());

        assertThatThrownBy(() -> facade.exchangeManual(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("insufficient funds");
    }

    private static CreateCurrencyExchangeRequest request(CurrencyTradeOperation operation,
                                                        TradeSource source) {
        return new CreateCurrencyExchangeRequest(
                1L,
                LocalDate.of(2026, 1, 15),
                USD,
                RUB,
                operation,
                new BigDecimal("100.00"),
                new BigDecimal("9000.00"),
                new BigDecimal("90.00"),
                "test",
                null,
                null,
                source);
    }

    private static CurrencyTradeEntity trade(CurrencyTradeOperation operation) {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        CurrencyTradeEntity trade = new CurrencyTradeEntity();
        trade.setPortfolio(portfolio);
        trade.setLotCurrency(USD);
        trade.setCounterCurrency(RUB);
        trade.setOperationType(operation);
        trade.setLotVolume(new BigDecimal("100.00"));
        trade.setCounterVolume(new BigDecimal("9000.00"));
        trade.setSource(TradeSource.MANUAL);
        return trade;
    }
}
