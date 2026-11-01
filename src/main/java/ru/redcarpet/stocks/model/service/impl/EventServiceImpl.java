package ru.redcarpet.stocks.model.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.CreateEventRequest;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.entity.Security;
import ru.redcarpet.stocks.model.enums.OperationType;
import ru.redcarpet.stocks.model.exception.ConflictException;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.repositories.EventRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;
import ru.redcarpet.stocks.model.repositories.SecurityRepository;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.EventService;
import ru.redcarpet.stocks.model.service.HoldingService;

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
    private final BalanceService balanceService;
    private final HoldingService holdingService;

    @Override
    @Transactional
    public void create(CreateEventRequest request) {
        Portfolio portfolio = portfolioRepository.findById(request.getPortfolioId())
                .orElseThrow(() -> new NotFoundException(
                        "Portfolio not found: " + request.getPortfolioId()));

        Security security = resolveSecurity(request);

        validate(request);

        Currency baseCurrency = portfolio.getBaseCurrency();
        BigDecimal exchangeRate = resolveExchangeRate(request, baseCurrency);

        BigDecimal amount = computeAmount(request);
        applyCashMovement(portfolio, request, amount);
        Event event = buildEvent(portfolio, security, request, exchangeRate, amount);
        saveInternal(event);
    }

    @Override
    @Transactional
    public Event save(Event event) {
        return saveInternal(event);
    }

    private Event saveInternal(Event event) {
        try {
            Event saved = eventRepository.save(event);
            holdingService.applyEvent(saved);
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Trade already exists: " + event.getTradeNumber(), e);
        }
    }

    @Override
    public boolean exists(Event event) {
        return event != null
                && event.getTradeNumber() != null
                && eventRepository.existsByTradeKey(
                event.getPortfolio().getId(),
                event.getTradeSource(),
                event.getTradeNumber(),
                event.getTradeDate(),
                event.getTradeIsin());
    }

    private Security resolveSecurity(CreateEventRequest request) {
        if (Objects.isNull(request.getTicker())) {
            if (request.getOperationType().affectsQuantity()) {
                throw new ValidationException(
                        "ticker is required for " + request.getOperationType()
                );
            }
            return null;
        }
        return securityRepository.findByTicker(request.getTicker())
                .orElseThrow(() -> new NotFoundException(
                        "Security not found: " + request.getTicker()
                ));
    }

    private void validate(CreateEventRequest request) {
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
                        "rate must be null or 1 when currency == baseCurrency");
            }
            return null;
        }

        if (req.getExchangeRate() == null || req.getExchangeRate().signum() <= 0) {
            throw new ValidationException(
                    "rate is required when currency != baseCurrency");
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

    private void applyCashMovement(Portfolio portfolio,
                                   CreateEventRequest req,
                                   BigDecimal amount
    ) {
        OperationType type = req.getOperationType();
        if (!type.affectsCash()) return;

        Currency currency = req.getCurrency();

        switch (type.getCashDir()) {
            case IN -> balanceService.credit(portfolio, currency, amount);
            case OUT -> balanceService.debit(portfolio, currency, amount);
            case NONE -> { }
        }
    }

    private Event buildEvent(
            Portfolio portfolio,
            Security security,
            CreateEventRequest request,
            BigDecimal exchangeRate,
            BigDecimal amount
    ) {
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

        return event;
    }
}
