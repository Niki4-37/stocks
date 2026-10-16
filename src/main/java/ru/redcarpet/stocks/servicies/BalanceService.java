package ru.redcarpet.stocks.servicies;

import ru.redcarpet.stocks.entity.Portfolio;
import ru.redcarpet.stocks.entity.PortfolioBalance;

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
