package ru.redcarpet.stocks.verification.trades;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.repositories.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeVerificationService {

    private final EventRepository eventRepository;

    public TradesCheckResponse check(TradesCheckRequest request) {
        List<String> numbers = request.trades().stream()
                .map(TradeKeyDto::tradeNumber)
                .distinct()
                .toList();

        // существующие события по номерам (в рамках портфеля и источника)
        Map<String, List<Event>> byNumber = eventRepository
                .findByTradeNumbers(request.portfolioId(), request.tradeSource(), numbers)
                .stream()
                .collect(Collectors.groupingBy(Event::getTradeNumber));

        List<TradeCheckResult> results = request.trades().stream()
                .map(key -> checkTrade(key, byNumber.getOrDefault(key.tradeNumber(), List.of())))
                .toList();

        return new TradesCheckResponse(
                request.portfolioId(), results, summaryOf(results));
    }

    /** Сверяет одну сделку из отчёта с событиями, найденными в БД по тому же номеру. */
    private static TradeCheckResult checkTrade(TradeKeyDto key, List<Event> candidates) {
        if (candidates.isEmpty()) {
            return result(key, TradeMatchStatus.MISSING, null, List.of());
        }

        return findExact(candidates, key)
                .map(exact -> result(key, TradeMatchStatus.MATCHED, exact.getId(), List.of()))
                .orElseGet(() -> conflict(key, candidates.get(0)));
    }

    private static Optional<Event> findExact(List<Event> candidates, TradeKeyDto key) {
        return candidates.stream()
                .filter(e -> Objects.equals(e.getTradeDate(), key.tradeDate()))
                .filter(e -> Objects.equals(e.getTradeIsin(), key.tradeIsin()))
                .findFirst();
    }

    /** Номер совпал, но дата или ISIN отличаются — показываем расхождения по первому событию. */
    private static TradeCheckResult conflict(TradeKeyDto key, Event stored) {
        return result(key, TradeMatchStatus.CONFLICT, stored.getId(), differences(key, stored));
    }

    private static List<String> differences(TradeKeyDto key, Event stored) {
        List<String> diffs = new ArrayList<>(2);

        if (!Objects.equals(stored.getTradeDate(), key.tradeDate())) {
            diffs.add("tradeDate: db=" + stored.getTradeDate()
                    + ", report=" + key.tradeDate());
        }
        if (!Objects.equals(stored.getTradeIsin(), key.tradeIsin())) {
            diffs.add("tradeIsin: db=" + stored.getTradeIsin()
                    + ", report=" + key.tradeIsin());
        }
        return diffs;
    }

    private static TradeCheckResult result(TradeKeyDto key,
                                           TradeMatchStatus status,
                                           Long eventId,
                                           List<String> diffs) {
        return new TradeCheckResult(
                key.tradeNumber(), key.tradeDate(), key.tradeIsin(),
                status, eventId, diffs);
    }

    private static Summary summaryOf(List<TradeCheckResult> results) {
        return new Summary(
                results.size(),
                count(results, TradeMatchStatus.MATCHED),
                count(results, TradeMatchStatus.MISSING),
                count(results, TradeMatchStatus.CONFLICT));
    }

    private static int count(List<TradeCheckResult> results, TradeMatchStatus status) {
        return (int) results.stream()
                .filter(r -> r.status() == status)
                .count();
    }
}
