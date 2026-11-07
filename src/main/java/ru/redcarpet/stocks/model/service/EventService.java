package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.CreateEventRequest;
import ru.redcarpet.stocks.model.entity.Event;

import java.math.BigDecimal;

public interface EventService {
    void create(CreateEventRequest request);
    void save(Event event);
    boolean exists(Event event);
    BigDecimal computeAmount(CreateEventRequest request);
}
