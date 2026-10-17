package ru.redcarpet.stocks.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.dto.CreateCashOperationRequest;
import ru.redcarpet.stocks.servicies.CashOperationService;

@RestController
@RequestMapping("/api/cash-operations")
@RequiredArgsConstructor
public class CashOperationController {

    private final CashOperationService cashOperationService;

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody CreateCashOperationRequest request
    ) {
        cashOperationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cash operation created!");
    }
}
