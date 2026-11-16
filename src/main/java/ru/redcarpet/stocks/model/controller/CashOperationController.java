package ru.redcarpet.stocks.model.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.application.CashOperationFacade;
import ru.redcarpet.stocks.model.dto.CreateCashOperationCommand;
import ru.redcarpet.stocks.model.dto.CreateCashOperationRequest;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.verification.cash.CashCheckRequest;
import ru.redcarpet.stocks.verification.cash.CashCheckResponse;
import ru.redcarpet.stocks.verification.cash.CashVerificationService;
import ru.redcarpet.stocks.verification.trades.TradeSource;

@RestController
@RequestMapping("/api/cash-operations")
@RequiredArgsConstructor
public class CashOperationController {

    private final CashOperationFacade cashOperationFacade;
    private final CashVerificationService verificationService;

    @PostMapping
    public ResponseEntity<Long> create(
            @Valid @RequestBody CreateCashOperationRequest req
    ) {
        CreateCashOperationCommand command = new CreateCashOperationCommand(
                req.portfolioId(),
                req.operationType(),
                req.direction(),
                req.operationDate(),
                req.currency(),
                req.amount(),
                req.platform(),
                TradeSource.MANUAL,
                req.comment(),
                null
        );
        CashOperation operation = cashOperationFacade.createManual(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(operation.getId());
    }

    @PostMapping("/check")
    public CashCheckResponse check(@Valid @RequestBody CashCheckRequest request) {
        return verificationService.check(request);
    }
}
