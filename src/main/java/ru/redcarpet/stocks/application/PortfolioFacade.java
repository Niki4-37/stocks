package ru.redcarpet.stocks.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.CreatePortfolioCommand;
import ru.redcarpet.stocks.model.dto.PortfolioBasicInfo;
import ru.redcarpet.stocks.model.dto.PortfolioDetails;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CashOperationService;
import ru.redcarpet.stocks.model.service.PortfolioService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioFacade {

    private final PortfolioService portfolioService;
    private final BalanceService balanceService;
    private final CashOperationService cashOperationService;

    @Transactional
    public PortfolioBasicInfo create(CreatePortfolioCommand cmd) {
        Portfolio portfolio = portfolioService.create(cmd);
        BigDecimal initial = cmd.initialBalance();

        if (initial != null && initial.signum() > 0) {
            balanceService.credit(portfolio.getId(), cmd.baseCurrency(), initial);
        } else {
            balanceService.recalculate(portfolio);
        }

        return new PortfolioBasicInfo(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getBaseCurrency(),
                portfolio.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<PortfolioBasicInfo> list() {
        return portfolioService.findAllPortfolios().stream()
                .map(p -> new PortfolioBasicInfo(p.id(), p.name(), p.baseCurrency(), p.createdAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioDetails details(Long id) {
        return portfolioService.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PortfolioDetails.Balance> balances(Long id) {
        return portfolioService.getPortfolioBalances(id);
    }
}