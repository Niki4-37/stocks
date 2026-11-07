package ru.redcarpet.stocks.model.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.model.dto.CreateCurrencyExchangeRequest;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CurrencyService;

@RestController
@RequestMapping("/api/currency-exchanges")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;
    private final BalanceService balanceService;

    @PostMapping
    public ResponseEntity<Long> exchange(
            @Valid @RequestBody CreateCurrencyExchangeRequest request
    ) {
        currencyService.exchange(request);

        if (request.isManual()) {
            if (request.isBuying()) {
                balanceService.debit(request.portfolioId(), request.counterCurrency(), request.resolvedCounterVolume());
                balanceService.credit(request.portfolioId(), request.lotCurrency(), request.resolvedLotVolume());
            } else {
                balanceService.debit(request.portfolioId(), request.lotCurrency(), request.resolvedLotVolume());
                balanceService.credit(request.portfolioId(), request.counterCurrency(), request.resolvedCounterVolume());
            }
        }

        return ResponseEntity.ok(request.portfolioId());
    }
}
