package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.entity.PortfolioBalance;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public interface BalanceService {

    /** Зачислить сумму (положительную) */
    void credit(Portfolio portfolio, Currency currency, BigDecimal amount);

    /** Списать сумму (положительную). Бросает исключение при недостатке */
    void debit(Portfolio portfolio, Currency currency, BigDecimal amount);

    /** Текущий баланс по валюте (0, если записи нет) */
    BigDecimal getBalance(Portfolio portfolio, Currency currency);

    /** Все балансы портфеля */
    List<PortfolioBalance> getAll(Portfolio portfolio);
}
