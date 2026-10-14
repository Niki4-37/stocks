package ru.redcarpet.stocks.servicies;

import ru.redcarpet.stocks.dto.CreateEventRequest;

public interface EventService {
    void create(CreateEventRequest request);
}
