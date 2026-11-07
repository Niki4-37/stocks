package ru.redcarpet.stocks.model.dto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public record PortfolioDetails(
        Long id,
        String name,
        Currency baseCurrency,
        List<Balance> balances,
        List<HeldSecurity> securities,
        Map<Currency, BigDecimal> cashByCurrency,
        Map<Currency, BigDecimal> costBasisByCurrency,
        // агрегаты по позициям, в базовой валюте портфеля
        BigDecimal totalCostBasis,
        BigDecimal totalMarketValue,   // null, пока нет цен
        BigDecimal totalCashValue,     // сумма балансов в базовой валюте
        BigDecimal totalPortfolioValue // cash + market (или cash + costBasis, пока нет цен)
) {
    public record Balance(Currency currency, BigDecimal amount) {}

    /** Бумага во владении (включая закрытые позиции — для учёта средней стоимости). */
    public record HeldSecurity(
            String ticker,
            String name,
            BigDecimal quantity,       // 0 для закрытых позиций
            BigDecimal avgPrice,       // сохраняется даже при quantity = 0
            Currency currency,
            BigDecimal totalDividends,
            BigDecimal totalCoupons,
            BigDecimal realizedPnl,
            BigDecimal costBasis
    ) {}
}
