package ru.redcarpet.stocks.model.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.CreatePortfolioCommand;
import ru.redcarpet.stocks.model.entity.Holding;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.exception.ConflictException;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.repositories.HoldingRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioBalanceRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;
import ru.redcarpet.stocks.model.service.PortfolioService;
import ru.redcarpet.stocks.model.dto.PortfolioDetails;
import ru.redcarpet.stocks.model.dto.PortfolioBasicInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioBalanceRepository balanceRepository;
    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;

    @Override
    @Transactional
    public Portfolio create(CreatePortfolioCommand request) {
        Portfolio portfolio = new Portfolio();
        portfolio.setName(request.name().trim());
        portfolio.setBaseCurrency(request.baseCurrency());

        try {
            return portfolioRepository.saveAndFlush(portfolio);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    "Portfolio with name '" + request.name() + "' already exists");
        }
    }

    @Override
    public List<PortfolioDetails.Balance> getPortfolioBalances(Long id) {
        return balanceRepository.findAllByPortfolioId(id).stream()
                .map(pb -> new PortfolioDetails.Balance(pb.getCurrency(), pb.getAmount()))
                .toList();
    }

    @Override
    public List<PortfolioBasicInfo> findAllPortfolios() {
        return portfolioRepository.findAll().stream()
                .map(portfolio -> new PortfolioBasicInfo(
                        portfolio.getId(),
                        portfolio.getName(),
                        portfolio.getBaseCurrency(),
                        portfolio.getCreatedAt()))
                .toList();
    }

    @Override
    public PortfolioDetails findById(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Portfolio not found: " + id));

        List<PortfolioDetails.Balance> balances = getPortfolioBalances(id);

        List<Holding> rawHoldings = holdingRepository.findByPortfolioId(id);

        List<PortfolioDetails.HeldSecurity> holdings = rawHoldings.stream()
                .map(this::toHoldingResponse)
                .toList();

        return new PortfolioDetails(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getBaseCurrency(),
                balances,
                holdings);
    }

    private PortfolioDetails.HeldSecurity toHoldingResponse(Holding h) {
        BigDecimal costBasis = h.getCostBasis().setScale(2, RoundingMode.HALF_UP);

        return new PortfolioDetails.HeldSecurity(
                h.getSecurity().getTicker(),
                h.getSecurity().getName(),
                h.getQuantity(),
                h.getAvgPrice(),
                h.getCurrency(),
                h.getTotalDividends(),
                h.getTotalCoupons(),
                h.getRealizedPnl(),
                costBasis);
    }
}
