package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.CreateEventRequest;
import ru.redcarpet.stocks.model.entity.Event;

public interface EventService {
    void create(CreateEventRequest request);
    Event save(Event event);
    boolean exists(Event event);
}
