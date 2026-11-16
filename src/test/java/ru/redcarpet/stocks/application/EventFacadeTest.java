package ru.redcarpet.stocks.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.redcarpet.stocks.model.dto.CreateEventRequest;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.enums.OperationType;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.EventService;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventFacadeTest {

    private static final Currency RUB = Currency.getInstance("RUB");

    @Mock
    private EventService eventService;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private EventFacade facade;

    @Test
    void debitCashOnBuy() {
        CreateEventRequest request = mock(CreateEventRequest.class);
        when(eventService.create(request)).thenReturn(event(OperationType.BUY, "100.00"));

        facade.create(request);

        assertApplied("-100.00");
    }

    @Test
    void creditCashOnDividend() {
        CreateEventRequest request = mock(CreateEventRequest.class);
        when(eventService.create(request)).thenReturn(event(OperationType.DIVIDEND, "100.00"));

        facade.create(request);

        assertApplied("100.00");
    }

    @Test
    void doNotTouchBalanceWhenOperationHasNoCashEffect() {
        CreateEventRequest request = mock(CreateEventRequest.class);
        when(eventService.create(request)).thenReturn(event(OperationType.TRANSFER_IN, "0.00"));

        facade.create(request);

        verify(balanceService, never()).apply(anyLong(), any(), any());
    }

    @Test
    void propagateBalanceFailure() {
        CreateEventRequest request = mock(CreateEventRequest.class);
        when(eventService.create(request)).thenReturn(event(OperationType.BUY, "100.00"));
        doThrow(new ValidationException("insufficient funds"))
                .when(balanceService).apply(eq(1L), eq(RUB), any());

        assertThatThrownBy(() -> facade.create(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("insufficient funds");
    }

    private void assertApplied(String expected) {
        ArgumentCaptor<BigDecimal> amount = ArgumentCaptor.forClass(BigDecimal.class);
        verify(balanceService).apply(eq(1L), eq(RUB), amount.capture());
        assertThat(amount.getValue()).isEqualByComparingTo(expected);
    }

    private static Event event(OperationType type, String amount) {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);

        Event event = new Event();
        event.setPortfolio(portfolio);
        event.setOperationType(type);
        event.setCurrency(RUB);
        event.setAmount(new BigDecimal(amount));
        return event;
    }
}
