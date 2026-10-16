package ru.redcarpet.stocks.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.dto.BalanceResponse;
import ru.redcarpet.stocks.dto.CreatePortfolioRequest;
import ru.redcarpet.stocks.servicies.PortfolioService;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody CreatePortfolioRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully created!");
    }

    @GetMapping("/{id}/balances")
    public List<BalanceResponse> getBalance(@PathVariable("id") Long id) {
        return portfolioService.getBalances(id);
    }
}
