package ru.redcarpet.stocks.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.CreateEventRequest;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.EventService;

import java.math.BigDecimal;

/**
 * Оркестратор ручного создания события: событие и движение денег
 * выполняются в одной транзакции.
 */
@Service
@RequiredArgsConstructor
public class EventFacade {

    private final EventService eventService;
    private final BalanceService balanceService;

    @Transactional
    public Event create(CreateEventRequest request) {
        Event event = eventService.create(request);

        if (event.getOperationType().affectsCash()) {
            int sign = event.getOperationType().getCashDir().sign();
            balanceService.apply(
                    event.getPortfolio().getId(),
                    event.getCurrency(),
                    event.getAmount().multiply(BigDecimal.valueOf(sign)));
        }

        return event;
    }
}
