package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.BalanceMismatch;
import ru.redcarpet.stocks.model.entity.Portfolio;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public interface BalanceService {

    /** Движение на счёте (credit/debit). */
    void apply(Long portfolioId, Currency currency, BigDecimal delta);

    /** Зачислить сумму (положительную) */
    default void credit(Long portfolioId, Currency currency, BigDecimal amount) {
        apply(portfolioId, currency, amount);
    }

    /** Списать сумму (положительную). Бросает исключение при недостатке */
    default void debit(Long portfolioId, Currency currency, BigDecimal amount) {
        apply(portfolioId, currency, amount.negate());
    }

    /** Пересчёт кэша из cash_operations. */
    void recalculate(Portfolio portfolio);

    /** Сверка кэша с ожидаемым. */
    List<BalanceMismatch> check(Long portfolioId);
}
