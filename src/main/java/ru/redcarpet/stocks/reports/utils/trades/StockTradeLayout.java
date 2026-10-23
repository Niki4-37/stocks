package ru.redcarpet.stocks.reports.utils.trades;

import ru.redcarpet.stocks.reports.dto.SecurityRef;
import ru.redcarpet.stocks.reports.dto.StockTrade;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;

public final class StockTradeLayout extends SecurityTradeLayout<StockTrade> {

    @Override
    public boolean supports(TradeSubsection subsection) {
        return subsection == TradeSubsection.STOCK;
    }

    @Override
    protected StockTrade build(SecurityRef s,
                               String date, String number,
                               String boughtQty, String buyPrice, String paymentAmount,
                               String soldQty, String sellPrice, String proceeds,
                               String currency, String deliveryFact) {
        return new StockTrade(
                s.ticker(), s.isin(), s.regNumber(), s.name(),
                date, number,
                boughtQty, buyPrice, paymentAmount,
                soldQty, sellPrice, proceeds,
                currency, deliveryFact
        );
    }
}