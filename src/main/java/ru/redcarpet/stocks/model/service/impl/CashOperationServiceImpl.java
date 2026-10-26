package ru.redcarpet.stocks.model.service.impl;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.CreateCashOperationRequest;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.repositories.CashOperationRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CashOperationService;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CashOperationServiceImpl implements CashOperationService {

    private final CashOperationRepository cashOperationRepository;
    private final PortfolioRepository portfolioRepository;
    private final BalanceService balanceService;

    @Override
    @Transactional
    public void create(CreateCashOperationRequest request) {
        Objects.requireNonNull(request, "Create cash operation request is null");

        CashOperationType type = request.operationType();
        if (type.isExchange()) {
            throw new ValidationException(
                    "Use /api/currency-exchanges for CURRENCY_EXCHANGE");
        }

        Portfolio portfolio = portfolioRepository.findById(request.portfolioId())
                .orElseThrow(() -> new NotFoundException(
                        "Portfolio not found: " + request.portfolioId()));

        BigDecimal amount = request.amount();
        if (Objects.isNull(amount) || amount.signum() <= 0) {
            throw new ValidationException("amount must be > 0");
        }

        // движение денег
        switch (type.getDirection()) {
            case INCOME  -> balanceService.credit(portfolio, request.currency(), amount);
            case EXPENSE -> balanceService.debit(portfolio, request.currency(), amount);
        }

        CashOperation op = newCashOperation(portfolio, request);

        CashOperation saved = cashOperationRepository.save(op);
    }

    private @NonNull CashOperation newCashOperation(Portfolio portfolio, CreateCashOperationRequest request) {
        CashOperation op = new CashOperation();
        op.setPortfolio(portfolio);
        op.setOperationType(request.operationType());
        op.setOperationDate(request.operationDate());
        op.setCurrency(request.currency());
        op.setAmount(request.amount());
        op.setComment(request.comment());

        return op;
    }
}
