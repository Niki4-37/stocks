package ru.redcarpet.stocks.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.CreateCurrencyExchangeRequest;
import ru.redcarpet.stocks.model.entity.CurrencyTradeEntity;
import ru.redcarpet.stocks.model.enums.CurrencyTradeOperation;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CurrencyService;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;

/**
 * Оркестратор ручных валютных сделок: сделка и два движения денег
 * выполняются в одной транзакции.
 */
@Service
@RequiredArgsConstructor
public class CurrencyFacade {

    private final CurrencyService currencyService;
    private final BalanceService balanceService;

    @Transactional
    public CurrencyTradeEntity exchangeManual(CreateCurrencyExchangeRequest request) {
        if (request.source() != null && request.source() != TradeSource.MANUAL) {
            throw new ValidationException(
                    "Only MANUAL currency trades can be created via API");
        }

        CurrencyTradeEntity trade = currencyService.exchange(request);

        BigDecimal lotVolume = trade.getLotVolume();
        BigDecimal counterVolume = trade.getCounterVolume();
        Long portfolioId = trade.getPortfolio().getId();

        if (trade.getOperationType() == CurrencyTradeOperation.BUY) {
            balanceService.debit(portfolioId, trade.getCounterCurrency(), counterVolume);
            balanceService.credit(portfolioId, trade.getLotCurrency(), lotVolume);
        } else {
            balanceService.debit(portfolioId, trade.getLotCurrency(), lotVolume);
            balanceService.credit(portfolioId, trade.getCounterCurrency(), counterVolume);
        }

        return trade;
    }
}
