package ru.redcarpet.stocks.reports.trades;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.reports.currency.CurrencyIdentityParser;
import ru.redcarpet.stocks.reports.currency.CurrencyTradeLayout;
import ru.redcarpet.stocks.reports.dto.BondTrade;
import ru.redcarpet.stocks.reports.dto.CurrencyIdentityFromReport;
import ru.redcarpet.stocks.reports.dto.CurrencyTrade;
import ru.redcarpet.stocks.reports.dto.FundTrade;
import ru.redcarpet.stocks.reports.dto.SecurityIdentityFromReport;
import ru.redcarpet.stocks.reports.dto.StockTrade;
import ru.redcarpet.stocks.reports.dto.TradeParseResult;
import ru.redcarpet.stocks.reports.dto.TradesResult;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;

import java.util.ArrayList;
import java.util.List;

/** Раздел 2.1 — сделки (акции, паи, валюта; облигации/овернайт — позже). */
@Component
public final class TradesParser {

    public TradeParseResult parse(List<Row> rows, DataFormatter fmt) {
        List<StockTrade> stocks = new ArrayList<>();
        List<FundTrade> funds = new ArrayList<>();
        List<BondTrade> bonds = new ArrayList<>();
        List<CurrencyTrade> currencies = new ArrayList<>();

        TradeLayout<?> paperLayout = null;
        CurrencyTradeLayout currencyLayout = null;
        SecurityIdentityFromReport currentPaper = SecurityIdentityFromReport.EMPTY;
        CurrencyIdentityFromReport currentCurrency = CurrencyIdentityFromReport.EMPTY;
        TradeSubsectionClassifier subsectionClassifier = new TradeSubsectionClassifier();

        for (Row row : rows) {
            TradeSubsection sub = subsectionClassifier.classify(row, fmt);

            if (sub == TradeSubsection.CURRENCY) {
                // ── валюта ──
                if (currencyLayout == null) {
                    currencyLayout = new CurrencyTradeLayout();
                    currentCurrency = CurrencyIdentityFromReport.EMPTY;
                }

                CurrencyIdentityFromReport identity = CurrencyIdentityParser.tryParse(row, fmt);
                if (identity != null) {
                    currentCurrency = identity;
                    continue;
                }

                if (currencyLayout.tryUpdate(row, fmt)) continue;
                if (!currencyLayout.isValid()) continue;

                CurrencyTrade c = currencyLayout.toTrade(row, fmt, currentCurrency);
                if (c != null) currencies.add(c);
                continue;
            }

            // ── бумаги ──
            if (paperLayout == null || !paperLayout.isSupports(sub)) {
                paperLayout = TradeLayoutFactory.create(sub);
                currentPaper = SecurityIdentityFromReport.EMPTY;
                if (paperLayout == null) continue;
            }

            SecurityIdentityFromReport identity = SecurityIdentityParser.tryParse(row, fmt);
            if (identity != null) {
                currentPaper = identity;
                continue;
            }

            if (paperLayout.tryUpdate(row, fmt)) continue;
            if (!paperLayout.isValid()) continue;

            Object trade = paperLayout.toTrade(row, fmt, currentPaper);
            if (trade instanceof StockTrade s) stocks.add(s);
            else if (trade instanceof FundTrade f) funds.add(f);
            else if (trade instanceof BondTrade b) bonds.add(b);
        }

        return new TradeParseResult(
                new TradesResult(stocks, funds, bonds),
                currencies);
    }
}