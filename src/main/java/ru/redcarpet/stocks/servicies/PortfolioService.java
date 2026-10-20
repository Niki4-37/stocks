package ru.redcarpet.stocks.servicies;

import ru.redcarpet.stocks.dto.BalanceResponse;
import ru.redcarpet.stocks.dto.CreatePortfolioRequest;
import ru.redcarpet.stocks.ui.dto.PortfolioResponse;

import java.util.Currency;
import java.util.List;

public interface PortfolioService {
    Currency getBaseCurrency();
    void create(CreatePortfolioRequest request);
    List<BalanceResponse> getBalances(Long id);
    List<PortfolioResponse> findAll();
}
