package ru.redcarpet.stocks.importing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.importing.dto.ImportResult;
import ru.redcarpet.stocks.model.component.EventFactory;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.repositories.EventRepository;
import ru.redcarpet.stocks.model.service.HoldingService;
import ru.redcarpet.stocks.reports.dto.TradesResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class TradeImportService {

    private final EventFactory eventFactory;
    private final EventRepository eventRepository;
    private final HoldingService holdingService;

    @Transactional
    public ImportResult importTrades(Portfolio portfolio, TradesResult trades) {
        ImportResult stocks = importAll(portfolio, trades.stocks(), eventFactory::fromStockTrade);
        ImportResult funds  = importAll(portfolio, trades.funds(),  eventFactory::fromFundTrade);
        ImportResult bonds  = importAll(portfolio, trades.bonds(),  eventFactory::fromBondTrade);
        return stocks.merge(funds).merge(bonds);
    }

    private <T> ImportResult importAll(Portfolio portfolio,
                              List<T> trades,
                              BiFunction<T, Portfolio, Event> toEvent) {
        int inserted = 0;
        int skipped = 0;
        List<String> warnings = new ArrayList<>();

        for (T trade : trades) {
            Event event;
            try {
                event = toEvent.apply(trade, portfolio);
            } catch (RuntimeException e) {
                warnings.add("Bad trade " + trade + ": " + e.getMessage());
                skipped++;
                continue;
            }

            if (isAlreadyImported(portfolio.getId(), event)) {
                skipped++;
                continue;
            }

            eventRepository.save(event);
            holdingService.applyEvent(event);
            inserted++;
        }

        return new ImportResult(inserted, skipped, warnings);
    }

    private boolean isAlreadyImported(Long portfolioId, Event event) {
        return Objects.nonNull(event)
                && Objects.nonNull(event.getTradeNumber())
                && eventRepository.existsByTradeKey(
                        portfolioId,
                        event.getTradeSource(),
                        event.getTradeNumber(),
                        event.getTradeDate(),
                        event.getTradeIsin());
    }
}