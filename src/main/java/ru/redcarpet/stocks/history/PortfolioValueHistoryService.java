package ru.redcarpet.stocks.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.CurrencySum;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.repositories.CashOperationRepository;
import ru.redcarpet.stocks.model.repositories.EventRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PortfolioValueHistoryService {

    private final CashOperationRepository cashOperationRepository;
    private final EventRepository eventRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional(readOnly = true)
    public List<DailyBalance> history(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException("Portfolio not found: " + portfolioId));
        Currency base = portfolio.getBaseCurrency();

        // 1) границы: min/max по операциям и событиям
        LocalDate from = earliestDate(portfolioId);
        LocalDate to = latestDate(portfolioId);
        if (from == null) return List.of();

        // 2) загрузить всё, что нужно
        List<CashOperation> cashOps = cashOperationRepository
                .findByPortfolioIdAndOperationDateBetweenOrderByOperationDateAsc(
                        portfolioId, from, to);

        List<Event> events = eventRepository
                .findByPortfolioIdAndEventDateBetweenOrderByEventDateAsc(
                        portfolioId, from, to);

        // 3) начальный баланс денег на `from` — сумма операций строго до from
        Map<Currency, BigDecimal> cash = initialCashBefore(portfolioId, from);

        // 4) начальные позиции на `from` — восстановить из событий до from
        Map<Long, PositionState> positions = initialPositionsBefore(portfolioId, from);

        // 5) пройти по дням
        List<DailyBalance> result = new ArrayList<>();
        int ci = 0, ei = 0;

        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {

            // применить cash_operations этого дня
            while (ci < cashOps.size() && cashOps.get(ci).getOperationDate().isEqual(d)) {
                CashOperation op = cashOps.get(ci);
                BigDecimal delta = op.getDirection() == CashFlowDirection.INCOME
                        ? op.getAmount()
                        : op.getAmount().negate();
                cash.merge(op.getCurrency(), delta, BigDecimal::add);
                ci++;
            }

            // применить events этого дня (BUY/SELL) — меняют позиции
            while (ei < events.size() && events.get(ei).getEventDate().isEqual(d)) {
                applyEvent(positions, events.get(ei));
                ei++;
            }

            // собрать DailyBalance
            result.add(buildDailyBalance(d, cash, positions, base));
        }
        return result;
    }

    private void applyEvent(Map<Long, PositionState> positions, Event event) {
        if (event.getSecurity() == null) return;   // дивиденды/купоны без бумаги — не здесь

        Long secId = event.getSecurity().getId();
        PositionState state = positions.computeIfAbsent(secId, id -> new PositionState(
                event.getSecurity(), event.getCurrency()));

        switch (event.getOperationType()) {
            case BUY, TRANSFER_IN -> state.buy(event);
            case SELL, TRANSFER_OUT -> state.sell(event);
            default -> { /* DIVIDEND/COUPON — только в Holding, не в позициях */ }
        }
    }

    private DailyBalance buildDailyBalance(LocalDate d,
                                           Map<Currency, BigDecimal> cash,
                                           Map<Long, PositionState> positions,
                                           Currency base) {
        List<DailyBalance.Position> posList = positions.values().stream()
                .filter(p -> p.quantity.signum() != 0)
                .map(PositionState::toDto)
                .toList();

        BigDecimal totalCash = cash.getOrDefault(base, BigDecimal.ZERO);
        BigDecimal totalCost = posList.stream()
                .filter(p -> p.currency().equals(base))   // пока только базовая
                .map(DailyBalance.Position::costBasis)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DailyBalance(
                d,
                new HashMap<>(cash),   // копия, чтобы не менялась после
                posList,
                totalCash.setScale(2, RoundingMode.HALF_UP),
                totalCost.setScale(2, RoundingMode.HALF_UP),
                totalCash.add(totalCost).setScale(2, RoundingMode.HALF_UP));
    }

    // ---- helpers ----

    private LocalDate earliestDate(Long portfolioId) {
        LocalDate c = cashOperationRepository.findMinOperationDateByPortfolioId(portfolioId).orElse(null);
        LocalDate e = eventRepository.findMinEventDateByPortfolioId(portfolioId).orElse(null);
        return Stream.of(c, e).filter(Objects::nonNull).min(LocalDate::compareTo).orElse(null);
    }

    private LocalDate latestDate(Long portfolioId) {
        LocalDate c = cashOperationRepository.findMaxOperationDateByPortfolioId(portfolioId).orElse(null);
        LocalDate e = eventRepository.findMaxEventDateByPortfolioId(portfolioId).orElse(null);
        return Stream.of(c, e).filter(Objects::nonNull).max(LocalDate::compareTo).orElse(null);
    }

    private Map<Currency, BigDecimal> initialCashBefore(Long portfolioId, LocalDate date) {
        // один SQL: SUM по валютам, где operationDate < date
        return cashOperationRepository.sumByCurrencyBeforeDate(portfolioId, date).stream()
                .collect(Collectors.toMap(
                        CurrencySum::getCurrency,
                        CurrencySum::getAmount));
    }

    private Map<Long, PositionState> initialPositionsBefore(Long portfolioId, LocalDate date) {
        Map<Long, PositionState> result = new HashMap<>();
        for (Event event : eventRepository
                .findByPortfolioIdAndEventDateBeforeOrderByEventDateAsc(portfolioId, date)) {
            applyEvent(result, event);
        }
        return result;
    }
}