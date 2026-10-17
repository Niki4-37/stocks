package ru.redcarpet.stocks.servicies.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.entity.Event;
import ru.redcarpet.stocks.entity.Holding;
import ru.redcarpet.stocks.repositories.HoldingRepository;
import ru.redcarpet.stocks.servicies.HoldingService;

@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements HoldingService {

    private static final int AVG_PRICE_SCALE = 6;

    private final HoldingRepository holdingRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void applyEvent(Event event) {
        Long portfolioId = event.getPortfolio().getId();
        Long securityId = event.getSecurity().getId();

        Holding holding = holdingRepository
                .findByPortfolioIdAndSecurityId(portfolioId, securityId)
                .orElseGet(() -> newHolding(event));

        holding.applyEvent(event);

        holdingRepository.save(holding);
    }

    private Holding newHolding(Event event) {
        Holding h = new Holding();
        h.setPortfolio(event.getPortfolio());
        h.setSecurity(event.getSecurity());
        h.setCurrency(event.getCurrency());
        return h;
    }
}
