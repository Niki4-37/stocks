package ru.redcarpet.stocks.model.dto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public record PortfolioDetailsResponse(
        Long id,
        String name,
        Currency baseCurrency,
        List<BalanceResponse> balances,
        List<HoldingResponse> holdings,
        Map<Currency, BigDecimal> cashByCurrency,
        Map<Currency, BigDecimal> costBasisByCurrency,
        // агрегаты по позициям, в базовой валюте портфеля
        BigDecimal totalCostBasis,
        BigDecimal totalMarketValue,   // null, пока нет цен
        BigDecimal totalCashValue,     // сумма балансов в базовой валюте
        BigDecimal totalPortfolioValue // cash + market (или cash + costBasis, пока нет цен)
) {}
