package ru.redcarpet.stocks.reports.trades;

import ru.redcarpet.stocks.reports.dto.SecurityIdentityFromReport;
import ru.redcarpet.stocks.reports.dto.StockTrade;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;

public final class StockTradeLayout extends SecurityTradeLayout<StockTrade> {

    @Override
    public boolean isSupports(TradeSubsection subsection) {
        return subsection == TradeSubsection.STOCK;
    }

    @Override
    protected StockTrade build(SecurityIdentityFromReport s,
                               String date, String number,
                               String boughtQty, String buyPrice, String paymentAmount,
                               String soldQty, String sellPrice, String proceeds,
                               String currency, String deliveryFact, String place) {
        return new StockTrade(
                s.ticker(), s.isin(), s.name(),
                date, number,
                boughtQty, buyPrice, paymentAmount,
                soldQty, sellPrice, proceeds,
                currency, deliveryFact, place
        );
    }
}