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
import ru.redcarpet.stocks.application.PortfolioFacade;
import ru.redcarpet.stocks.model.dto.CreatePortfolioCommand;
import ru.redcarpet.stocks.model.dto.PortfolioDetails;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioFacade facade;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody CreatePortfolioCommand request) {
        facade.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Portfolio successfully created!");
    }

    @GetMapping("/{id}/balances")
    public List<PortfolioDetails.Balance> getBalances(@PathVariable("id") Long id) {
        return facade.balances(id);
    }
}
