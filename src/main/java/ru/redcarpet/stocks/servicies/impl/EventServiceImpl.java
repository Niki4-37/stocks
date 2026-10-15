package ru.redcarpet.stocks.servicies.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.redcarpet.stocks.dto.CreateEventRequest;
import ru.redcarpet.stocks.entity.Event;
import ru.redcarpet.stocks.entity.Portfolio;
import ru.redcarpet.stocks.entity.Security;
import ru.redcarpet.stocks.exception.NotFoundException;
import ru.redcarpet.stocks.exception.ValidationException;
import ru.redcarpet.stocks.repositories.EventRepository;
import ru.redcarpet.stocks.repositories.PortfolioRepository;
import ru.redcarpet.stocks.repositories.SecurityRepository;
import ru.redcarpet.stocks.servicies.EventService;
import ru.redcarpet.stocks.servicies.HoldingService;
import ru.redcarpet.stocks.servicies.PortfolioService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final PortfolioRepository portfolioRepository;
    private final SecurityRepository securityRepository;
    private final PortfolioService portfolioService;
    private final HoldingService holdingService;

    @Override
    @Transactional
    public void create(CreateEventRequest request) {
        Portfolio portfolio = portfolioRepository.findById(request.getPortfolioId())
                .orElseThrow(() -> new NotFoundException(
                        "Portfolio not found: " + request.getPortfolioId()));

        Security security = securityRepository.findByTicker(request.getTicker())
                .orElseThrow(() -> new NotFoundException(
                        "Security not found: " + request.getTicker()));

        validateByOperationType(request);

        Currency baseCurrency = portfolio.getBaseCurrency();
        BigDecimal exchangeRate = resolveExchangeRate(request, baseCurrency);

        BigDecimal amount = computeAmount(request);
        BigDecimal amountInBase = computeAmountInBase(amount, exchangeRate);

        Event event = new Event();
        event.setPortfolio(portfolio);
        event.setSecurity(security);
        event.setOperationType(request.getOperationType());
        event.setEventDate(request.getEventDate());
        event.setPrice(request.getPrice());
        event.setCurrency(request.getCurrency());
        event.setExchangeRate(exchangeRate);
        event.setQuantity(request.getQuantity());
        event.setAmount(amount);
        event.setAmountInBaseCurrency(amountInBase);

        Event saved = eventRepository.save(event);

        // обновляем позицию в той же транзакции
        if (saved.getOperationType().affectsQuantity()) {
            holdingService.applyEvent(saved);
        }
    }

    private void validateByOperationType(CreateEventRequest request) {
        boolean needsQty = request.getOperationType().affectsQuantity();

        if (needsQty) {
            if (Objects.isNull(request.getQuantity()) || request.getQuantity().signum() <= 0) {
                throw new ValidationException(
                        "quantity must be > 0 for " + request.getOperationType()
                );
            }
            if (Objects.isNull(request.getPrice()) || request.getPrice().signum() <= 0) {
                throw new ValidationException(
                        "price must be > 0 for " + request.getOperationType());
            }
        } else if (Objects.nonNull(request.getQuantity())) {
            throw new ValidationException(
                    "quantity must be null for " + request.getOperationType());
        }
    }

    private BigDecimal resolveExchangeRate(CreateEventRequest req, Currency baseCurrency) {
        boolean isBase = req.getCurrency().equals(baseCurrency);

        if (isBase) {
            if (req.getExchangeRate() != null
                    && req.getExchangeRate().compareTo(BigDecimal.ONE) != 0) {
                throw new ValidationException(
                        "exchangeRate must be null or 1 when currency == baseCurrency");
            }
            return null;
        }

        if (req.getExchangeRate() == null || req.getExchangeRate().signum() <= 0) {
            throw new ValidationException(
                    "exchangeRate is required when currency != baseCurrency");
        }
        return req.getExchangeRate();
    }

    private BigDecimal computeAmount(CreateEventRequest req) {
        BigDecimal price = req.getPrice();
        if (req.getQuantity() == null) {
            return price.setScale(2, RoundingMode.HALF_UP);
        }
        return price.multiply(req.getQuantity()).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeAmountInBase(BigDecimal amount, BigDecimal exchangeRate) {
        if (exchangeRate == null) {
            return amount;
        }
        return amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }
}
