package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.BalanceResponse;
import ru.redcarpet.stocks.model.dto.CreatePortfolioRequest;
import ru.redcarpet.stocks.ui.dto.PortfolioDetailsResponse;
import ru.redcarpet.stocks.ui.dto.PortfolioResponse;

import java.util.Currency;
import java.util.List;

public interface PortfolioService {
    Currency getBaseCurrency();
    void create(CreatePortfolioRequest request);
    List<BalanceResponse> getBalances(Long id);
    List<PortfolioResponse> findAll();
    PortfolioDetailsResponse findById(Long id);
}
