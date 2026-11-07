package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.CreatePortfolioCommand;
import ru.redcarpet.stocks.model.dto.PortfolioDetails;
import ru.redcarpet.stocks.model.dto.PortfolioBasicInfo;
import ru.redcarpet.stocks.model.entity.Portfolio;

import java.util.List;

public interface PortfolioService {
    Portfolio create(CreatePortfolioCommand request);
    List<PortfolioDetails.Balance> getPortfolioBalances(Long id);
    List<PortfolioBasicInfo> findAllPortfolios();
    PortfolioDetails findById(Long id);
}
