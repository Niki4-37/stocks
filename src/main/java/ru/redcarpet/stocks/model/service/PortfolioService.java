package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.BalanceResponse;
import ru.redcarpet.stocks.model.dto.CreatePortfolioRequest;
import ru.redcarpet.stocks.model.dto.PortfolioDetailsResponse;
import ru.redcarpet.stocks.model.dto.PortfolioResponse;
import ru.redcarpet.stocks.model.entity.Portfolio;

import java.util.List;

public interface PortfolioService {
    Portfolio create(CreatePortfolioRequest request);
    List<BalanceResponse> getPortfolioBalances(Long id);
    List<PortfolioResponse> findAllPortfolios();
    PortfolioDetailsResponse findById(Long id);
}
