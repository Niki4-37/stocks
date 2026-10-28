package ru.redcarpet.stocks.model.service.impl;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.CreateCashOperationRequest;
import ru.redcarpet.stocks.model.dto.CreateCashOperationCommand;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.repositories.CashOperationRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CashOperationService;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CashOperationServiceImpl implements CashOperationService {

    private final CashOperationRepository cashOperationRepository;
    private final PortfolioRepository portfolioRepository;
    private final BalanceService balanceService;

    @Override
    @Transactional
    public CashOperation create(CreateCashOperationCommand command) {
        Objects.requireNonNull(command, "Create cash operation command is null");

        if (command.operationType().isExchange()) {
            throw new ValidationException(
                    "Use /api/currency-exchanges for CURRENCY_EXCHANGE");
        }

        Portfolio portfolio = portfolioRepository.findById(command.portfolioId())
                .orElseThrow(() -> new NotFoundException(
                        "Portfolio not found: " + command.portfolioId()));

        BigDecimal amount = command.amount();
        if (Objects.isNull(amount) || amount.signum() <= 0) {
            throw new ValidationException("amount must be > 0");
        }

        // движение денег
        switch (command.direction()) {
            case INCOME  -> balanceService.credit(portfolio, command.currency(), amount);
            case EXPENSE -> balanceService.debit(portfolio, command.currency(), amount);
        }

        CashOperation op = newCashOperation(portfolio, command);

        return cashOperationRepository.save(op);
    }

    @Override
    public long count(Long portfolioId, TradeSource source,
                      LocalDate date, CashOperationType type,
                      String platform, CashFlowDirection direction,
                      BigDecimal amount, Currency currency) {
        return cashOperationRepository
                .countByPortfolioIdAndSourceAndOperationDateAndOperationTypeAndPlatformAndDirectionAndAmountAndCurrency(
                        portfolioId, source, date, type, platform, direction, amount, currency);
    }

    private @NonNull CashOperation newCashOperation(
            Portfolio portfolio,
            CreateCashOperationCommand command
    ) {
        CashOperation op = new CashOperation();
        op.setPortfolio(portfolio);
        op.setOperationType(command.operationType());
        op.setDirection(command.direction());
        op.setOperationDate(command.operationDate());
        op.setCurrency(command.currency());
        op.setAmount(command.amount());
        op.setComment(command.comment());
        op.setPlatform(command.platform());
        op.setSource(Objects.nonNull(command.source()) ? command.source() : TradeSource.MANUAL);

        if (command.dividend() != null) {
            op.setDividendIssuer(command.dividend().issuer());
            op.setDividendIsin(command.dividend().isin());
            op.setDividendRecordDate(command.dividend().recordDate());
            op.setDividendTax(command.dividend().tax());
        }

        return op;
    }
}
