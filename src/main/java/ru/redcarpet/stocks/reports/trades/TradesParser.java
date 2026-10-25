package ru.redcarpet.stocks.reports.trades;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.reports.dto.CurrencyTrade;
import ru.redcarpet.stocks.reports.dto.FundTrade;
import ru.redcarpet.stocks.reports.dto.SecurityRef;
import ru.redcarpet.stocks.reports.dto.StockTrade;
import ru.redcarpet.stocks.reports.dto.TradesResult;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Раздел 2.1 — сделки (акции, паи, валюта; облигации/овернайт — позже). */
@Component
public final class TradesParser {

    public TradesResult parse(List<Row> rows, DataFormatter fmt) {
        List<StockTrade> stocks = new ArrayList<>();
        List<FundTrade> funds = new ArrayList<>();
        List<CurrencyTrade> currencies = new ArrayList<>();

        TradeLayout<?> layout = null;
        SecurityRef current = SecurityRef.EMPTY;
        TradeSubsectionClassifier subsectionClassifier = new TradeSubsectionClassifier();

        for (Row row : rows) {
            TradeSubsection sub = subsectionClassifier.classify(row, fmt);

            if (Objects.isNull(layout) || !layout.isSupports(sub)) {
                layout = switch (sub) {
                    case STOCK    -> new StockTradeLayout();
                    case FUND     -> new FundTradeLayout();
                    case CURRENCY -> new CurrencyTradeLayout();
                    case BOND, OVERNIGHT_CB, NONE -> null;
                };
                current = SecurityRef.EMPTY;
                if (Objects.isNull(layout)) continue;
            }

            SecurityRef parsed = (sub == TradeSubsection.CURRENCY)
                    ? CurrencyRefParser.tryParse(row, fmt)
                    : SecurityRefParser.tryParse(row, fmt);
            if (Objects.nonNull(parsed)) {
                current = parsed;
                continue;
            }

            if (layout.tryUpdate(row, fmt)) continue;
            if (!layout.isValid()) continue;

            Object trade = layout.toTrade(row, fmt, current);
            if (trade instanceof StockTrade s)         stocks.add(s);
            else if (trade instanceof FundTrade f)     funds.add(f);
            else if (trade instanceof CurrencyTrade c) currencies.add(c);
        }
        return new TradesResult(stocks, funds, currencies);
    }
}