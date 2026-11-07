package ru.redcarpet.stocks.model.service.impl;

import lombok.RequiredArgsConstructor;
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
import ru.redcarpet.stocks.model.service.PortfolioService;
import ru.redcarpet.stocks.model.dto.HoldingResponse;
import ru.redcarpet.stocks.model.dto.PortfolioDetailsResponse;
import ru.redcarpet.stocks.model.dto.PortfolioResponse;

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
    public Portfolio create(CreatePortfolioRequest request) {
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
    public List<BalanceResponse> getPortfolioBalances(Long id) {
        return balanceRepository.findAllByPortfolioId(id).stream()
                .map(pb -> new BalanceResponse(pb.getCurrency(), pb.getAmount()))
                .toList();
    }

    @Override
    public List<PortfolioResponse> findAllPortfolios() {
        return portfolioRepository.findAll().stream()
                .map(portfolio -> new PortfolioResponse(
                        portfolio.getId(),
                        portfolio.getName(),
                        portfolio.getBaseCurrency(),
                        portfolio.getCreatedAt()))
                .toList();
    }

    @Override
    public PortfolioDetailsResponse findById(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Portfolio not found: " + id));

        Currency base = portfolio.getBaseCurrency();

        List<BalanceResponse> balances = getPortfolioBalances(id);

        List<Holding> rawHoldings = holdingRepository.findByPortfolioId(id);

        Map<Currency, BigDecimal> cashByCurrency = sumByCurrency(
                base,
                balances,
                BalanceResponse::currency,
                BalanceResponse::amount
        );

        Map<Currency, BigDecimal> costBasisByCurrency = sumByCurrency(
            base,
            rawHoldings,
            Holding::getCurrency,
            Holding::getCostBasis
        );

        // агрегаты только по базовой валюте
        BigDecimal totalCostBasis = costBasisByCurrency.getOrDefault(base, BigDecimal.ZERO);

        BigDecimal totalCashValue = cashByCurrency.getOrDefault(base, BigDecimal.ZERO);
        // рыночной стоимости пока нет
        BigDecimal totalMarketValue = null;

        // пока цен нет — «стоимость портфеля» = cash + себестоимость
        BigDecimal totalPortfolioValue = totalCashValue.add(totalCostBasis)
                .setScale(2, RoundingMode.HALF_UP);

        List<HoldingResponse> holdings = rawHoldings.stream()
                .map(h -> toHoldingResponse(h, null))
                .toList();

        return new PortfolioDetailsResponse(
                portfolio.getId(),
                portfolio.getName(),
                base,
                balances,
                holdings,
                cashByCurrency,
                costBasisByCurrency,
                totalCostBasis,
                totalMarketValue,
                totalCashValue,
                totalPortfolioValue);
    }

    private HoldingResponse toHoldingResponse(Holding h, BigDecimal currentPrice) {
        BigDecimal costBasis = h.getCostBasis().setScale(2, RoundingMode.HALF_UP);

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

    private static <T> Map<Currency, BigDecimal> sumByCurrency(
            Currency base,
            Collection<T> items,
            Function<T, Currency> currencyExtractor,
            Function<T, BigDecimal> amountExtractor
    ) {
        Comparator<Currency> byBaseFirst = (a, b) -> {
            if (a.equals(b)) return 0;
            if (a.equals(base)) return -1;
            if (b.equals(base)) return 1;
            return a.getCurrencyCode().compareTo(b.getCurrencyCode());
        };
        Map<Currency, BigDecimal> result = new TreeMap<>(byBaseFirst);
        for (T item : items) {
            Currency currency = currencyExtractor.apply(item);
            BigDecimal amount = amountExtractor.apply(item);
            if (Objects.isNull(currency) || Objects.isNull(amount)) continue;
            result.merge(currency, amount, BigDecimal::add);
        }
        result.replaceAll((c, v) -> v.setScale(2, RoundingMode.HALF_UP));
        return result;
    }
}
