package ru.redcarpet.stocks.verification.trades;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.repositories.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeVerificationService {

    private final EventRepository eventRepository;

    public CheckTradesResponse check(CheckTradesRequest request) {
        List<String> numbers = request.trades().stream()
                .map(TradeKeyDto::tradeNumber)
                .distinct()
                .toList();

        // существующие события по номерам (в рамках портфеля и источника)
        Map<String, List<Event>> byNumber = eventRepository
                .findByTradeNumbers(request.portfolioId(), request.tradeSource(), numbers)
                .stream()
                .collect(Collectors.groupingBy(Event::getTradeNumber));

        List<TradeCheckResult> results = new ArrayList<>();
        int matched = 0, missing = 0, conflict = 0;

        for (TradeKeyDto key : request.trades()) {
            List<Event> candidates = byNumber.getOrDefault(key.tradeNumber(), List.of());

            if (candidates.isEmpty()) {
                results.add(new TradeCheckResult(
                        key.tradeNumber(), key.tradeDate(), key.tradeIsin(),
                        TradeMatchStatus.MISSING, null, List.of()));
                missing++;
                continue;
            }

            Event exact = candidates.stream()
                    .filter(e -> Objects.equals(e.getTradeDate(), key.tradeDate()))
                    .filter(e -> Objects.equals(e.getTradeIsin(), key.tradeIsin()))
                    .findFirst()
                    .orElse(null);

            if (exact != null) {
                results.add(new TradeCheckResult(
                        key.tradeNumber(), key.tradeDate(), key.tradeIsin(),
                        TradeMatchStatus.MATCHED, exact.getId(), List.of()));
                matched++;
            } else {
                List<String> diffs = new ArrayList<>();
                Event first = candidates.get(0);
                if (!Objects.equals(first.getTradeDate(), key.tradeDate())) {
                    diffs.add("tradeDate: db=" + first.getTradeDate()
                            + ", report=" + key.tradeDate());
                }
                if (!Objects.equals(first.getTradeIsin(), key.tradeIsin())) {
                    diffs.add("tradeIsin: db=" + first.getTradeIsin()
                            + ", report=" + key.tradeIsin());
                }
                results.add(new TradeCheckResult(
                        key.tradeNumber(), key.tradeDate(), key.tradeIsin(),
                        TradeMatchStatus.CONFLICT, first.getId(), diffs));
                conflict++;
            }
        }

        return new CheckTradesResponse(
                request.portfolioId(), results,
                new Summary(results.size(), matched, missing, conflict));
    }
}
