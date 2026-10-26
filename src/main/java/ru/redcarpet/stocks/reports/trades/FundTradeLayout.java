package ru.redcarpet.stocks.reports.trades;

import ru.redcarpet.stocks.reports.dto.FundTrade;
import ru.redcarpet.stocks.reports.dto.SecurityRef;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;

public final class FundTradeLayout extends SecurityTradeLayout<FundTrade> {

    @Override
    public boolean isSupports(TradeSubsection subsection) {
        return subsection == TradeSubsection.FUND;
    }

    @Override
    protected FundTrade build(SecurityRef s,
                              String date, String number,
                              String boughtQty, String buyPrice, String paymentAmount,
                              String soldQty, String sellPrice, String proceeds,
                              String currency, String deliveryFact) {
        return new FundTrade(
                s.ticker(), s.isin(), s.name(),
                date, number,
                boughtQty, buyPrice, paymentAmount,
                soldQty, sellPrice, proceeds,
                currency, deliveryFact
        );
    }
}