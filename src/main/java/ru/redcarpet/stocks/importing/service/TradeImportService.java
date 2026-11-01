package ru.redcarpet.stocks.importing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.importing.dto.ImportResult;
import ru.redcarpet.stocks.model.component.EventFactory;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.service.EventService;
import ru.redcarpet.stocks.reports.dto.TradesResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeImportService {

    private final EventFactory eventFactory;
    private final EventService eventService;

    @Transactional
    public ImportResult importTrades(Portfolio portfolio, TradesResult trades) {
        return importAll(portfolio, trades.stocks(), eventFactory::fromStockTrade)
                .merge(importAll(portfolio, trades.funds(),  eventFactory::fromFundTrade))
                .merge(importAll(portfolio, trades.bonds(),  eventFactory::fromBondTrade));
    }

    private <T> ImportResult importAll(
        Portfolio portfolio,
        List<T> trades,
        BiFunction<T, Portfolio, Event> toEvent
    ) {
        int inserted = 0;
        int skipped = 0;
        List<String> warnings = new ArrayList<>();

        for (T trade : trades) {
            try {
                Event event = toEvent.apply(trade, portfolio);

                if (eventService.exists(event)) {
                    skipped++;
                    continue;
                }

                eventService.save(event);
                inserted++;
            } catch (RuntimeException e) {
                log.warn("Failed to import trade {}: {}", trade, e.getMessage(), e);
                warnings.add("Bad trade " + trade + ": " + e.getMessage());
                skipped++;
                continue;
            }
        }

        return new ImportResult(inserted, skipped, warnings);
    }
}