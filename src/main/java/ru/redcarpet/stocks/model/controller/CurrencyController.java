package ru.redcarpet.stocks.model.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.model.dto.CreateCurrencyExchangeRequest;
import ru.redcarpet.stocks.model.entity.CurrencyTradeEntity;
import ru.redcarpet.stocks.model.enums.CurrencyTradeOperation;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CurrencyService;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/currency-exchanges")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;
    private final BalanceService balanceService;

    // TODO: вынести оркестрацию баланса в сервис-оркестратор (следующий коммит).
    @PostMapping
    @Transactional
    public ResponseEntity<Long> exchange(
            @Valid @RequestBody CreateCurrencyExchangeRequest request
    ) {
        CurrencyTradeEntity trade = currencyService.exchange(request);

        if (trade.getSource() == TradeSource.MANUAL) {
            BigDecimal lotVolume = trade.getLotVolume();
            BigDecimal counterVolume = trade.getCounterVolume();

            if (trade.getOperationType() == CurrencyTradeOperation.BUY) {
                balanceService.debit(request.portfolioId(), trade.getCounterCurrency(), counterVolume);
                balanceService.credit(request.portfolioId(), trade.getLotCurrency(), lotVolume);
            } else {
                balanceService.debit(request.portfolioId(), trade.getLotCurrency(), lotVolume);
                balanceService.credit(request.portfolioId(), trade.getCounterCurrency(), counterVolume);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(trade.getId());
    }
}
