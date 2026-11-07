package ru.redcarpet.stocks.model.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.BalanceMismatch;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.entity.PortfolioBalance;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.repositories.CashOperationRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioBalanceRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CashOperationService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final PortfolioBalanceRepository balanceRepository;
    private final CashOperationRepository cashOperationRepository;
    private final PortfolioRepository portfolioRepository;
    private final CashOperationService cashOperationService;

    @Override
    @Transactional
    public void apply(Long portfolioId, Currency currency, BigDecimal delta) {
        if (currency == null || delta == null || delta.signum() == 0) return;

        PortfolioBalance balance = getOrCreate(portfolioId, currency);
        BigDecimal newAmount = balance.getAmount().add(delta);
        if (newAmount.signum() < 0) {
            throw new ValidationException(String.format(
                    "Insufficient funds: required %s %s, available %s %s",
                    delta.negate(), currency.getCurrencyCode(),
                    balance.getAmount(), currency.getCurrencyCode()));
        }
        balance.setAmount(newAmount);
        balanceRepository.save(balance);
    }

    @Override
    @Transactional
    public void recalculate(Portfolio portfolio) {
        Map<Currency, BigDecimal> expected = expectedBalances(portfolio.getId());
        balanceRepository.deleteByPortfolioId(portfolio.getId());
        expected.forEach((currency, amount) -> {
            PortfolioBalance b = new PortfolioBalance();
            b.setPortfolio(portfolio);
            b.setCurrency(currency);
            b.setAmount(amount);
            balanceRepository.save(b);
        });
    }

    @Override
    public List<BalanceMismatch> check(Long portfolioId) {
        Map<Currency, BigDecimal> expected = expectedBalances(portfolioId);
        List<PortfolioBalance> actual = balanceRepository.findAllByPortfolioId(portfolioId);
        List<BalanceMismatch> mismatches = new ArrayList<>();

        // кэш vs ожидаемое
        Set<Currency> seen = new HashSet<>();
        for (PortfolioBalance b : actual) {
            seen.add(b.getCurrency());
            BigDecimal exp = expected.getOrDefault(b.getCurrency(), BigDecimal.ZERO);
            if (b.getAmount().compareTo(exp) != 0) {
                mismatches.add(new BalanceMismatch(b.getCurrency(), b.getAmount(), exp));
            }
        }
        // валюты в expected, но нет в кэше
        for (var e : expected.entrySet()) {
            if (seen.contains(e.getKey())) continue;
            if (e.getValue().signum() != 0) {
                mismatches.add(new BalanceMismatch(e.getKey(), BigDecimal.ZERO, e.getValue()));
            }
        }

        return mismatches;
    }

    private PortfolioBalance getOrCreate(Long portfolioId, Currency currency) {
        return balanceRepository
                .findByPortfolioIdAndCurrencyForUpdate(portfolioId, currency)
                .orElseGet(() -> {
                    PortfolioBalance pb = new PortfolioBalance();
                    pb.setPortfolio(portfolioRepository.getReferenceById(portfolioId));
                    pb.setCurrency(currency);
                    pb.setAmount(BigDecimal.ZERO);
                    return pb;
                });
    }

    private Map<Currency, BigDecimal> expectedBalances(Long portfolioId) {
        Map<Currency, BigDecimal> result = new HashMap<>();
        for (CashOperation op : cashOperationRepository.findByPortfolioId(portfolioId)) {
            BigDecimal delta = op.getDirection() == CashFlowDirection.INCOME
                    ? op.getAmount()
                    : op.getAmount().negate();
            result.merge(op.getCurrency(), delta, BigDecimal::add);
        }
        return result;
    }
}
