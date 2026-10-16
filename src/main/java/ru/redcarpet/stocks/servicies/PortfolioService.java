package ru.redcarpet.stocks.servicies;

import ru.redcarpet.stocks.dto.BalanceResponse;

import java.util.Currency;
import java.util.List;

public interface PortfolioService {
    Currency getBaseCurrency();

    List<BalanceResponse> getBalances(Long id);
}
