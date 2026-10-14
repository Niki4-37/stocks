package ru.redcarpet.stocks.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.dto.CreatePortfolioRequest;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    @PostMapping
    public ResponseEntity<String> create(@RequestBody CreatePortfolioRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully created!");
    }
}
