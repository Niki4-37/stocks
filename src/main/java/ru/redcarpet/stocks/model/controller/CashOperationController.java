package ru.redcarpet.stocks.model.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.model.dto.CreateCashOperationRequest;
import ru.redcarpet.stocks.model.service.CashOperationService;
import ru.redcarpet.stocks.verification.cash.CashVerificationService;
import ru.redcarpet.stocks.verification.cash.CashCheckRequest;
import ru.redcarpet.stocks.verification.cash.CashCheckResponse;

@RestController
@RequestMapping("/api/cash-operations")
@RequiredArgsConstructor
public class CashOperationController {

    private final CashOperationService cashOperationService;
    private final CashVerificationService verificationService;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody CreateCashOperationRequest request
    ) {
        cashOperationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cash operation created!");
    }

    @PostMapping("/check")
    public CashCheckResponse check(@Valid @RequestBody CashCheckRequest request) {
        return verificationService.check(request);
    }
}
