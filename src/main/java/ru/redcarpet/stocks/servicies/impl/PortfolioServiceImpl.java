package ru.redcarpet.stocks.servicies.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.redcarpet.stocks.dto.BalanceResponse;
import ru.redcarpet.stocks.dto.CreatePortfolioRequest;
import ru.redcarpet.stocks.entity.Portfolio;
import ru.redcarpet.stocks.exception.ConflictException;
import ru.redcarpet.stocks.repositories.PortfolioBalanceRepository;
import ru.redcarpet.stocks.repositories.PortfolioRepository;
import ru.redcarpet.stocks.servicies.BalanceService;
import ru.redcarpet.stocks.servicies.PortfolioService;
import ru.redcarpet.stocks.ui.dto.PortfolioResponse;

import java.math.BigDecimal;
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

    @Override
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
        return List.of();
    }
}
