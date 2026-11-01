package ru.redcarpet.stocks.reports.dto;

import java.util.List;

public record TradesResult(
        List<StockTrade> stocks,
        List<FundTrade> funds,
        List<BondTrade> bonds
) {
}
