package ru.redcarpet.stocks.model.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.application.CurrencyFacade;
import ru.redcarpet.stocks.model.dto.CreateCurrencyExchangeRequest;

@RestController
@RequestMapping("/api/currency-exchanges")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyFacade currencyFacade;

    @PostMapping
    public ResponseEntity<Long> exchange(
            @Valid @RequestBody CreateCurrencyExchangeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(currencyFacade.exchangeManual(request).getId());
    }
}
