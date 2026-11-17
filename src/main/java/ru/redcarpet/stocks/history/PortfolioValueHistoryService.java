package ru.redcarpet.stocks.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.exchange.ExchangeRateService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PortfolioValueHistoryService {

    private final CashOperationRepository cashOperationRepository;
    private final EventRepository eventRepository;
    private final PortfolioRepository portfolioRepository;
    private final ExchangeRateService exchangeRateService;

    @Transactional
    public List<DailyBalance> history(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException("Portfolio not found: " + portfolioId));
        Currency base = portfolio.getBaseCurrency();

        // 1) границы: min/max по операциям и событиям
        Optional<LocalDate> start = earliestDate(portfolioId);
        if (start.isEmpty()) return List.of();

        LocalDate from = start.get();
        LocalDate to = latestDate(portfolioId).orElse(from);

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

        // 5) все валюты, которые встретятся в истории, — курсы грузим пакетно:
        //    один запрос в БД + добор у ЦБ только по краям периода
        Map<Currency, NavigableMap<LocalDate, BigDecimal>> rates =
                exchangeRateService.ratesToBase(currenciesOf(cash, cashOps, positions, events), base, from, to);

        // 6) пройти по дням
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
            result.add(buildDailyBalance(d, cash, positions, base, rates));
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
                                           Currency base,
                                           Map<Currency, NavigableMap<LocalDate, BigDecimal>> rates) {
        List<DailyBalance.Position> posList = positions.values().stream()
                .filter(p -> p.quantity.signum() != 0)
                .map(PositionState::toDto)
                .toList();

        BigDecimal totalCash = cash.entrySet().stream()
                .map(e -> toBase(e.getValue(), e.getKey(), base, d, rates))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = posList.stream()
                .map(p -> toBase(p.costBasis(), p.currency(), base, d, rates))
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

    /** Все валюты, которые встретятся в истории: деньги, позиции и события. */
    private static Set<Currency> currenciesOf(Map<Currency, BigDecimal> cash,
                                              List<CashOperation> cashOps,
                                              Map<Long, PositionState> positions,
                                              List<Event> events) {
        Set<Currency> currencies = new HashSet<>();
        currencies.addAll(cash.keySet());
        for (CashOperation op : cashOps) currencies.add(op.getCurrency());
        for (PositionState state : positions.values()) currencies.add(state.currency);
        for (Event event : events) {
            // DIVIDEND/COUPON не меняют позиции и деньги истории — их валюта не нужна
            if (event.getOperationType() != null && event.getOperationType().affectsQuantity()) {
                currencies.add(event.getCurrency());
            }
        }
        currencies.remove(null);
        return currencies;
    }

    /**
     * Переводит сумму в базовую валюту портфеля по последнему известному курсу
     * на или до даты (выходные/праздники). Базовая валюта конвертации не требует.
     */
    private BigDecimal toBase(BigDecimal amount,
                              Currency from,
                              Currency base,
                              LocalDate date,
                              Map<Currency, NavigableMap<LocalDate, BigDecimal>> rates) {
        if (amount == null || amount.signum() == 0) return BigDecimal.ZERO;
        if (from.equals(base)) return amount;

        NavigableMap<LocalDate, BigDecimal> byDate = rates.get(from);
        Map.Entry<LocalDate, BigDecimal> rate = byDate == null ? null : byDate.floorEntry(date);
        if (rate == null) {
            throw new NotFoundException(
                    "Exchange rate not found: " + from + "→" + base + " on " + date);
        }
        return amount.multiply(rate.getValue()).setScale(2, RoundingMode.HALF_UP);
    }

    private Optional<LocalDate> earliestDate(Long portfolioId) {
        return Stream.of(
                        cashOperationRepository.findMinOperationDateByPortfolioId(portfolioId),
                        eventRepository.findMinEventDateByPortfolioId(portfolioId))
                .flatMap(Optional::stream)
                .min(LocalDate::compareTo);
    }

    private Optional<LocalDate> latestDate(Long portfolioId) {
        return Stream.of(
                        cashOperationRepository.findMaxOperationDateByPortfolioId(portfolioId),
                        eventRepository.findMaxEventDateByPortfolioId(portfolioId))
                .flatMap(Optional::stream)
                .max(LocalDate::compareTo);
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