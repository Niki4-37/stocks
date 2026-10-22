package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.CreateEventRequest;

public interface EventService {
    void create(CreateEventRequest request);
}
