package ru.redcarpet.stocks.model.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.BalanceResponse;
import ru.redcarpet.stocks.model.dto.CreatePortfolioRequest;
import ru.redcarpet.stocks.model.entity.Holding;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.exception.ConflictException;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.repositories.HoldingRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioBalanceRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.PortfolioService;
import ru.redcarpet.stocks.ui.dto.HoldingResponse;
import ru.redcarpet.stocks.ui.dto.PortfolioDetailsResponse;
import ru.redcarpet.stocks.ui.dto.PortfolioResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private Currency baseCurrency;

    private final PortfolioBalanceRepository balanceRepository;
    private final PortfolioRepository portfolioRepository;
    private final BalanceService balanceService;
    private final HoldingRepository holdingRepository;

    @Override
    @Transactional
    public void create(CreatePortfolioRequest request) {
        Portfolio portfolio = new Portfolio();
        portfolio.setName(request.name().trim());
        portfolio.setBaseCurrency(request.baseCurrency());

        Portfolio saved;
        try {
            saved = portfolioRepository.saveAndFlush(portfolio);
        } catch (DataIntegrityViolationException e) {
            // сработал uq_portfolios_name
            throw new ConflictException(
                    "Portfolio with name '" + request.name() + "' already exists");
        }

        // начальный баланс, если задан
        BigDecimal initial = request.initialBalance();
        if (Objects.nonNull(initial) && initial.signum() > 0) {
            balanceService.credit(saved, request.baseCurrency(), initial);
        }
    }

    @Override
    public List<BalanceResponse> getBalances(Long id) {
        return balanceRepository.findAllByPortfolioId(id).stream()
                .map(pb -> new BalanceResponse(pb.getCurrency(), pb.getAmount()))
                .toList();
    }

    @Override
    public List<PortfolioResponse> findAll() {
        return portfolioRepository.findAll().stream()
                .map(portfolio -> {
                    return new PortfolioResponse(
                            portfolio.getId(),
                            portfolio.getName(),
                            portfolio.getBaseCurrency(),
                            portfolio.getCreatedAt());})
                .toList();
    }

    @Override
    public PortfolioDetailsResponse findById(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Portfolio not found: " + id));

        Currency base = portfolio.getBaseCurrency();

        List<BalanceResponse> balances = balanceService.getAll(portfolio).stream()
                .map(b -> new BalanceResponse(b.getCurrency(), b.getAmount()))
                .toList();

        List<Holding> rawHoldings = holdingRepository.findByPortfolioId(id);

        List<HoldingResponse> holdings = rawHoldings.stream()
                .map(h -> toHoldingResponse(h, null))  // currentPrice = null пока
                .toList();

        // агрегаты только по базовой валюте
        BigDecimal totalCostBasis = rawHoldings.stream()
                .filter(h -> h.getCurrency().equals(base))
                .map(h -> h.getQuantity().multiply(h.getAvgPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalCashValue = balances.stream()
                .filter(b -> b.currency().equals(base))
                .map(BalanceResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // рыночной стоимости пока нет
        BigDecimal totalMarketValue = null;

        // пока цен нет — «стоимость портфеля» = cash + себестоимость
        BigDecimal totalPortfolioValue = totalCashValue.add(totalCostBasis);

        return new PortfolioDetailsResponse(
                portfolio.getId(),
                portfolio.getName(),
                base,
                balances,
                holdings,
                totalCostBasis,
                totalMarketValue,
                totalCashValue,
                totalPortfolioValue);
    }

    private HoldingResponse toHoldingResponse(Holding h, BigDecimal currentPrice) {
        BigDecimal costBasis = h.getQuantity().multiply(h.getAvgPrice())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal marketValue = currentPrice == null
                ? null
                : h.getQuantity().multiply(currentPrice)
                .setScale(2, RoundingMode.HALF_UP);

        return new HoldingResponse(
                h.getSecurity().getTicker(),
                h.getSecurity().getName(),
                h.getQuantity(),
                h.getAvgPrice(),
                h.getCurrency(),
                h.getTotalDividends(),
                h.getTotalCoupons(),
                h.getRealizedPnl(),
                costBasis,
                currentPrice,
                marketValue);
    }
}
