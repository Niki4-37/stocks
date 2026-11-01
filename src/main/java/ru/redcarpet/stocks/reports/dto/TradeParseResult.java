package ru.redcarpet.stocks.reports.dto;

import java.util.List;

public record TradeParseResult(
        TradesResult papers,
        List<CurrencyTrade> currencies
) {
}
