package ru.redcarpet.stocks.history;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/value-history")
@RequiredArgsConstructor
public class PortfolioHistoryController {

    private final PortfolioValueHistoryService service;

    @GetMapping
    public List<DailyBalance> history(@PathVariable Long portfolioId) {
        return service.history(portfolioId);
    }

    @GetMapping("/range")
    public List<DailyBalance> history(@PathVariable Long portfolioId,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.history(portfolioId);
    }
}