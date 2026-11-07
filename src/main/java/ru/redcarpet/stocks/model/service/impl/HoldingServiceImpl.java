package ru.redcarpet.stocks.model.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.entity.Holding;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.repositories.HoldingRepository;
import ru.redcarpet.stocks.model.service.HoldingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository repository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void applyEvent(Event event) {
        Long portfolioId = event.getPortfolio().getId();
        Long securityId = event.getSecurity().getId();

        Holding holding = repository
                .findByPortfolioIdAndSecurityId(portfolioId, securityId)
                .orElseGet(() -> newHolding(event));

        holding.applyEvent(event);

        repository.save(holding);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void applyDividend(
            Long portfolioId,
            Long securityId,
            BigDecimal amount,
            Currency currency,
            LocalDate date
    ) {
        Holding holding = repository
                .findByPortfolioIdAndSecurityId(portfolioId, securityId)
                .orElseThrow(() -> new NotFoundException(
                        "Holding not found for portfolio " + portfolioId + " / security " + securityId));
        holding.applyDividend(amount, currency, date);
        repository.save(holding);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void applyCoupon(
            Long portfolioId,
            Long securityId,
            BigDecimal amount,
            Currency currency,
            LocalDate date
    ) {
        Holding holding = repository
                .findByPortfolioIdAndSecurityId(portfolioId, securityId)
                .orElseThrow(() -> new NotFoundException("Holding not found for portfolio " + portfolioId + " / security " + securityId));
        holding.applyCoupon(amount, currency, date);
        repository.save(holding);
    }

    private Holding newHolding(Event event) {
        Holding h = new Holding();
        h.setPortfolio(event.getPortfolio());
        h.setSecurity(event.getSecurity());
        h.setCurrency(event.getCurrency());
        return h;
    }
}
