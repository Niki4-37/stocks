package ru.redcarpet.stocks.reports.trades;

import ru.redcarpet.stocks.reports.currency.CurrencyTradeLayout;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;

public final class TradeLayoutFactory {

    private TradeLayoutFactory() {}

    /**
     * @return стратегия для подраздела или null, если подраздел не обрабатывается
     */
    @SuppressWarnings("unchecked")
    public static <T> TradeLayout<T> create(TradeSubsection subsection) {
        return (TradeLayout<T>) switch (subsection) {
            case STOCK    -> new StockTradeLayout();
            case FUND     -> new FundTradeLayout();
            case BOND     -> new BondTradeLayout();
            case CURRENCY -> new CurrencyTradeLayout();
            case OVERNIGHT_CB, NONE -> null;
        };
    }
}