package ru.redcarpet.stocks.model.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.CreateCurrencyExchangeRequest;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.repositories.CashOperationRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CurrencyExchangeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    private final CashOperationRepository cashOperationRepository;
    private final PortfolioRepository portfolioRepository;
    private final BalanceService balanceService;

    @Override
    @Transactional
    public void exchange(CreateCurrencyExchangeRequest request) {
        Portfolio portfolio = portfolioRepository.findById(request.portfolioId())
                .orElseThrow(() -> new NotFoundException(
                        "Portfolio not found: " + request.portfolioId()));

        Currency source = request.sourceCurrency();
        Currency target = request.targetCurrency();

        if (source.equals(target)) {
            throw new ValidationException(
                    "source and target currency must differ");
        }

        // 1. Списываем source (проверка достаточности внутри debit)
        balanceService.debit(portfolio, source, request.sourceAmount());

        // 2. Зачисляем target
        balanceService.credit(portfolio, target, request.targetAmount());

        CashOperation op = newCashOperation(portfolio, request);
        CashOperation saved = cashOperationRepository.save(op);
    }

    private CashOperation newCashOperation(Portfolio portfolio, CreateCurrencyExchangeRequest request) {
        BigDecimal sourceAmount = request.sourceAmount();
        BigDecimal targetAmount = request.targetAmount();

        BigDecimal rate = request.exchangeRate();
        if (Objects.isNull(rate)) {
            rate = sourceAmount.divide(targetAmount, 8, RoundingMode.HALF_UP);
        }

        CashOperation op = new CashOperation();
        op.setPortfolio(portfolio);
        op.setOperationType(CashOperationType.CURRENCY_EXCHANGE);
        op.setOperationDate(request.operationDate());
        op.setCurrency(request.sourceCurrency());
        op.setAmount(request.sourceAmount());
        op.setTargetCurrency(request.targetCurrency());
        op.setTargetAmount(targetAmount);
        op.setExchangeRate(rate);
        op.setComment(request.comment());

        return op;
    }
}
