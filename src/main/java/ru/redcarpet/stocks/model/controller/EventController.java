package ru.redcarpet.stocks.model.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.model.dto.CreateEventRequest;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.EventService;

import java.math.BigDecimal;
import java.util.Currency;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final BalanceService balanceService;

    @PostMapping
    public ResponseEntity<Long> create(@Valid @RequestBody CreateEventRequest request) {
        eventService.create(request);

        if (request.getOperationType().affectsCash()) {
            BigDecimal amount = eventService.computeAmount(request);
            Currency currency = request.getCurrency();
            switch (request.getOperationType().getCashDir()) {
                case IN -> balanceService.credit(request.getPortfolioId(), currency, amount);
                case OUT -> balanceService.debit(request.getPortfolioId(), currency, amount);
                case NONE -> { }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(request.getPortfolioId());
    }
}
