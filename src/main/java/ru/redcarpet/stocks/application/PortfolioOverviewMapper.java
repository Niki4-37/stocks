package ru.redcarpet.stocks.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.exchange.ExchangeRateService;
import ru.redcarpet.stocks.model.dto.PortfolioDetails;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class PortfolioOverviewMapper {

    private final ExchangeRateService exchangeRateService;

    public PortfolioOverview toOverview(PortfolioDetails raw) {
        Currency base = raw.baseCurrency();
        LocalDate today = LocalDate.now();

        // агрегаты по валютам (в исходных валютах, для UI)
        Map<Currency, BigDecimal> cashByCurrency = sumByCurrency(
                base, raw.balances(),
                PortfolioDetails.Balance::currency,
                PortfolioDetails.Balance::amount);

        Map<Currency, BigDecimal> costBasisByCurrency = sumByCurrency(
                base, raw.securities(),
                PortfolioDetails.HeldSecurity::currency,
                PortfolioDetails.HeldSecurity::costBasis);

        // итоги в базовой валюте — с конвертацией
        BigDecimal totalCash = raw.balances().stream()
                .map(b -> convert(b.amount(), b.currency(), base, today))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalCost = raw.securities().stream()
                .map(h -> convert(h.costBasis(), h.currency(), base, today))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalMarket = null;   // пока нет цен
        BigDecimal totalValue = totalCash.add(totalCost).setScale(2, RoundingMode.HALF_UP);

        return new PortfolioOverview(
                raw,
                cashByCurrency,
                costBasisByCurrency,
                totalCost,
                totalMarket,
                totalCash,
                totalValue);
    }


    private BigDecimal convert(BigDecimal amount, Currency from, Currency to, LocalDate date) {
        if (amount == null || amount.signum() == 0) return BigDecimal.ZERO;
        if (from.equals(to)) return amount;
        // Не подменяем ошибку нулём: иначе валютные позиции молча исчезают из итогов.
        return exchangeRateService.convert(amount, from, to, date);
    }

    private static <T> Map<Currency, BigDecimal> sumByCurrency(
            Currency base,
            Collection<T> items,
            Function<T, Currency> currencyExtractor,
            Function<T, BigDecimal> amountExtractor
    ) {
        Comparator<Currency> byBaseFirst = (a, b) -> {
            if (a.equals(b)) return 0;
            if (a.equals(base)) return -1;
            if (b.equals(base)) return 1;
            return a.getCurrencyCode().compareTo(b.getCurrencyCode());
        };
        Map<Currency, BigDecimal> result = new TreeMap<>(byBaseFirst);
        for (T item : items) {
            Currency currency = currencyExtractor.apply(item);
            BigDecimal amount = amountExtractor.apply(item);
            if (Objects.isNull(currency) || Objects.isNull(amount)) continue;
            result.merge(currency, amount, BigDecimal::add);
        }
        result.replaceAll((c, v) -> v.setScale(2, RoundingMode.HALF_UP));
        return result;
    }
}