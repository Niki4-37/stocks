package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.entity.Event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

public interface HoldingService {
    void applyEvent(Event event);
    /** Применить дивиденд из 1.1 (без Event). */
    void applyDividend(Long portfolioId, Long securityId,
                       BigDecimal amount, Currency currency, LocalDate date);

    /** Применить купон из 1.1 (без Event). */
    void applyCoupon(Long portfolioId, Long securityId,
                     BigDecimal amount, Currency currency, LocalDate date);
}
