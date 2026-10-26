package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.CreateEventRequest;
import ru.redcarpet.stocks.verification.trades.TradeKeyDto;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.enums.OperationType;

import java.math.BigDecimal;
import java.util.Currency;

public interface EventService {
    void create(CreateEventRequest request);
    Event createFromTrade(TradeKeyDto key, Long portfolioId,
                          OperationType type, BigDecimal price, BigDecimal quantity,
                          Currency currency, BigDecimal amount);
}
