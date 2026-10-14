package ru.redcarpet.stocks.servicies.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.redcarpet.stocks.dto.CreateEventRequest;
import ru.redcarpet.stocks.entity.Event;
import ru.redcarpet.stocks.repositories.EventRepository;
import ru.redcarpet.stocks.servicies.EventService;
import ru.redcarpet.stocks.servicies.PortfolioService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@RequiredArgsConstructor
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final PortfolioService portfolioService;

    @Override
    public void create(CreateEventRequest request) {

    }

    private void recalcDerived(Event e, Currency baseCurrency) {
        BigDecimal amount = e.getQuantity() == null
                ? e.getPrice()
                : e.getPrice().multiply(e.getQuantity());
        e.setAmount(amount.setScale(2, RoundingMode.HALF_UP));

        BigDecimal inBase = e.getExchangeRate() == null
                ? amount
                : amount.multiply(e.getExchangeRate());
        e.setAmountInBaseCurrency(inBase.setScale(2, RoundingMode.HALF_UP));
    }
}
