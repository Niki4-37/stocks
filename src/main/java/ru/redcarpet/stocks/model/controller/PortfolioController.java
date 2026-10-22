package ru.redcarpet.stocks.model.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.model.dto.BalanceResponse;
import ru.redcarpet.stocks.model.dto.CreatePortfolioRequest;
import ru.redcarpet.stocks.model.service.PortfolioService;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody CreatePortfolioRequest request) {
        portfolioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Portfolio successfully created!");
    }

    @GetMapping("/{id}/balances")
    public List<BalanceResponse> getBalance(@PathVariable("id") Long id) {
        return portfolioService.getBalances(id);
    }
}
