package ru.redcarpet.stocks.importing.dto;

import java.util.ArrayList;
import java.util.List;

public record ImportReportResult(
        ImportResult cash,
        ImportResult trades,
        ImportResult currency
) {
    public int cashInserted()   { return cash.inserted(); }
    public int cashSkipped()    { return cash.skipped(); }
    public int tradesInserted() { return trades.inserted(); }
    public int tradesSkipped()  { return trades.skipped(); }
    public int currencyInserted() { return currency.inserted(); }
    public int currencySkipped()  { return currency.skipped(); }

    public List<String> warnings() {
        List<String> all = new ArrayList<>(cash.warnings());
        all.addAll(trades.warnings());
        all.addAll(currency.warnings());
        return all;
    }
}