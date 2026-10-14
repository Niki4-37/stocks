package ru.redcarpet.stocks.servicies;

import ru.redcarpet.stocks.entity.Event;

public interface HoldingService {
    void applyEvent(Event event);
}
