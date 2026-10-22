package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.entity.Event;

public interface HoldingService {
    void applyEvent(Event event);
}
