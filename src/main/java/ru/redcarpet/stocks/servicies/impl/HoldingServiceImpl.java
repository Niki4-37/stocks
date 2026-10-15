package ru.redcarpet.stocks.servicies.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.redcarpet.stocks.entity.Event;
import ru.redcarpet.stocks.entity.Holding;
import ru.redcarpet.stocks.exception.ValidationException;
import ru.redcarpet.stocks.repositories.HoldingRepository;
import ru.redcarpet.stocks.servicies.HoldingService;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements HoldingService {

    private static final int AVG_PRICE_SCALE = 6;

    private final HoldingRepository holdingRepository;

    @Override
    @Transactional
    public void applyEvent(Event event) {
        Long portfolioId = event.getPortfolio().getId();
        Long securityId = event.getSecurity().getId();

        Holding holding = holdingRepository
                .findByPortfolioIdAndSecurityId(portfolioId, securityId)
                .orElseGet(() -> newHolding(event));

        switch (event.getOperationType()) {
            case BUY, TRANSFER_IN   -> applyBuy(holding, event);
            case SELL, TRANSFER_OUT -> applySell(holding, event);
            default -> { /* сюда не попадаем, фильтр выше */ }
        }

        if (!(holding.getQuantity().signum() == 0)) {
            holdingRepository.save(holding);
        }
    }

    private Holding newHolding(Event event) {
        Holding h = new Holding();
        h.setPortfolio(event.getPortfolio());
        h.setSecurity(event.getSecurity());
        h.setCurrency(event.getCurrency());
        h.setQuantity(BigDecimal.ZERO);
        h.setAvgPrice(BigDecimal.ZERO);
        return h;
    }

    private void applyBuy(Holding h, Event e) {
        BigDecimal oldQty = h.getQuantity();
        BigDecimal buyQty = e.getQuantity();
        BigDecimal newQty = oldQty.add(buyQty);

        BigDecimal newAvg;
        if (oldQty.signum() == 0) {
            newAvg = e.getPrice();
        } else {
            newAvg = oldQty.multiply(h.getAvgPrice())
                    .add(buyQty.multiply(e.getPrice()))
                    .divide(newQty, AVG_PRICE_SCALE, RoundingMode.HALF_UP);
        }

        h.setQuantity(newQty);
        h.setAvgPrice(newAvg);
        h.setLastEventDate(e.getEventDate());
        if (h.getFirstBuyDate() == null) {
            h.setFirstBuyDate(e.getEventDate());
        }
    }

    private void applySell(Holding h, Event e) {
        BigDecimal sellQty = e.getQuantity();
        if (h.getQuantity().compareTo(sellQty) < 0) {
            throw new ValidationException(String.format(
                    "Cannot sell %s of %s: only %s held",
                    sellQty, h.getSecurity().getTicker(), h.getQuantity()));
        }
        h.setQuantity(h.getQuantity().subtract(sellQty));
        h.setLastEventDate(e.getEventDate());
        // avg_price не трогаем — метод средней стоимости
    }
}
