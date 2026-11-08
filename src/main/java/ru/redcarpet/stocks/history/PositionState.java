package ru.redcarpet.stocks.history;

import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.entity.Security;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

class PositionState {
    final Security security;
    final Currency currency;
    BigDecimal quantity = BigDecimal.ZERO;
    BigDecimal avgPrice = BigDecimal.ZERO;

    PositionState(Security security, Currency currency) {
        this.security = security;
        this.currency = currency;
    }

    void buy(Event e) {
        BigDecimal q = e.getQuantity();
        BigDecimal p = e.getPrice();
        if (q == null || q.signum() <= 0) return;

        BigDecimal newQty = quantity.add(q);
        if (quantity.signum() == 0) {
            avgPrice = p;
        } else {
            avgPrice = quantity.multiply(avgPrice)
                    .add(q.multiply(p))
                    .divide(newQty, 6, RoundingMode.HALF_UP);
        }
        quantity = newQty;
    }

    void sell(Event e) {
        BigDecimal q = e.getQuantity();
        if (q == null || q.signum() <= 0) return;
        if (quantity.compareTo(q) < 0) return;   // не должно быть, но защита
        quantity = quantity.subtract(q);
        // avgPrice не меняется при продаже (по FIFO/средней)
    }

    DailyBalance.Position toDto() {
        return new DailyBalance.Position(
                security.getId(),
                security.getTicker(),
                security.getName(),
                quantity,
                avgPrice,
                currency,
                quantity.multiply(avgPrice).setScale(2, RoundingMode.HALF_UP));
    }
}