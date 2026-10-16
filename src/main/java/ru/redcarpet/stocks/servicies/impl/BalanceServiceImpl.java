package ru.redcarpet.stocks.servicies.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.redcarpet.stocks.entity.Portfolio;
import ru.redcarpet.stocks.entity.PortfolioBalance;
import ru.redcarpet.stocks.exception.ValidationException;
import ru.redcarpet.stocks.repositories.PortfolioBalanceRepository;
import ru.redcarpet.stocks.servicies.BalanceService;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final PortfolioBalanceRepository balanceRepository;

    @Override
    @Transactional
    public void credit(Portfolio portfolio, Currency currency, BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new ValidationException("credit amount must be > 0");
        }
        PortfolioBalance balance = getOrCreate(portfolio, currency);
        balance.setAmount(balance.getAmount().add(amount));
        balanceRepository.save(balance);
    }

    @Override
    @Transactional
    public void debit(Portfolio portfolio, Currency currency, BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new ValidationException("debit amount must be > 0");
        }
        PortfolioBalance balance = getOrCreate(portfolio, currency);
        BigDecimal newAmount = balance.getAmount().subtract(amount);
        if (newAmount.signum() < 0) {
            throw new ValidationException(String.format(
                    "Insufficient funds: required %s %s, available %s %s",
                    amount, currency.getCurrencyCode(),
                    balance.getAmount(), currency.getCurrencyCode()));
        }
        balance.setAmount(newAmount);
        balanceRepository.save(balance);
    }

    @Override
    public BigDecimal getBalance(Portfolio portfolio, Currency currency) {
        return balanceRepository
                .findByPortfolioIdAndCurrencyForUpdate(portfolio.getId(), currency)
                .map(PortfolioBalance::getAmount)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public List<PortfolioBalance> getAll(Portfolio portfolio) {
        return balanceRepository.findAllByPortfolioId(portfolio.getId());
    }

    private PortfolioBalance getOrCreate(Portfolio portfolio, Currency currency) {
        return balanceRepository
                .findByPortfolioIdAndCurrencyForUpdate(portfolio.getId(), currency)
                .orElseGet(() -> {
                   PortfolioBalance pb = new PortfolioBalance();
                   pb.setPortfolio(portfolio);
                   pb.setCurrency(currency);
                   pb.setAmount(BigDecimal.ZERO);
                   return pb;
                });
    }
}
