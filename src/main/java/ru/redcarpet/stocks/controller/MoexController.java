package ru.redcarpet.stocks.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.redcarpet.stocks.servicies.MoexHistoryService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MoexController {

    private final MoexHistoryService service;

    @GetMapping("/{ticker}/history")
    public List<Map<String, Object>> history(
            @PathVariable(value = "ticker") String ticker,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate till,
            @RequestParam(defaultValue = "TQBR") String board
    ) {
        return service.getHistory(ticker, from, till, board);
    }
}
