package ru.redcarpet.stocks.ui.dto;

import ru.redcarpet.stocks.model.dto.BalanceResponse;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public record PortfolioDetailsResponse(
        Long id,
        String name,
        Currency baseCurrency,
        List<BalanceResponse> balances,
        List<HoldingResponse> holdings,

        // агрегаты по позициям, в базовой валюте портфеля
        BigDecimal totalCostBasis,
        BigDecimal totalMarketValue,   // null, пока нет цен
        BigDecimal totalCashValue,     // сумма балансов в базовой валюте
        BigDecimal totalPortfolioValue // cash + market (или cash + costBasis, пока нет цен)
) {}
