package ru.redcarpet.stocks.model.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.model.dto.CreateCurrencyExchangeRequest;
import ru.redcarpet.stocks.model.service.CurrencyExchangeService;

@RestController
@RequestMapping("/api/currency-exchanges")
@RequiredArgsConstructor
public class CurrencyExchangeController {

    private final CurrencyExchangeService currencyExchangeService;

    @PostMapping
    public ResponseEntity<String> exchange(
            @Valid @RequestBody CreateCurrencyExchangeRequest request
    ) {
        currencyExchangeService.exchange(request);
        return ResponseEntity.ok("Exchange currency handled!");
    }
}
