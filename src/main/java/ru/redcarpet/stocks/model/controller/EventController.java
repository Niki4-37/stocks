package ru.redcarpet.stocks.model.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.model.dto.CreateEventRequest;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.EventService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final BalanceService balanceService;

    // TODO: вынести оркестрацию баланса в сервис-оркестратор (следующий коммит).
    @PostMapping
    @Transactional
    public ResponseEntity<Long> create(@Valid @RequestBody CreateEventRequest request) {
        Event event = eventService.create(request);

        if (event.getOperationType().affectsCash()) {
            int sign = event.getOperationType().getCashDir().sign();
            balanceService.apply(
                    event.getPortfolio().getId(),
                    event.getCurrency(),
                    event.getAmount().multiply(BigDecimal.valueOf(sign)));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(event.getId());
    }
}
