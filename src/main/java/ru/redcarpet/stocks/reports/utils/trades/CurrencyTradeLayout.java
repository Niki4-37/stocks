package ru.redcarpet.stocks.reports.utils.trades;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.dto.CurrencyTrade;
import ru.redcarpet.stocks.reports.dto.SecurityRef;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;
import ru.redcarpet.stocks.reports.utils.CellReader;
import ru.redcarpet.stocks.reports.utils.HeaderColumnsMatcher;

import java.util.ArrayList;
import java.util.List;

public final class CurrencyTradeLayout implements TradeLayout<CurrencyTrade> {

    private static final String DATE        = "Дата";
    private static final String NUMBER      = "Номер";
    private static final String BUY_RATE    = "Курс сделки (покупка)";
    private static final String SELL_RATE   = "Курс сделки (продажа)";
    private static final String LOT_VOLUME  = "Объём в валюте лота";
    private static final String CNT_VOLUME  = "Объём в сопряж. валюте";

    private int dateCol = -1;
    private int numberCol = -1;
    private int buyRateCol = -1;
    private int sellRateCol = -1;
    private int buyLotCol = -1;
    private int sellLotCol = -1;
    private int buyCounterCol = -1;
    private int sellCounterCol = -1;
    private boolean valid;

    @Override
    public boolean isSupports(TradeSubsection subsection) {
        return subsection == TradeSubsection.CURRENCY;
    }

    @Override
    public boolean tryUpdate(Row row, DataFormatter fmt) {
        int cBuyRate  = HeaderColumnsMatcher.findExact(row, fmt, BUY_RATE);
        int cSellRate = HeaderColumnsMatcher.findExact(row, fmt, SELL_RATE);
        if (cBuyRate < 0 && cSellRate < 0) return false;

        this.buyRateCol  = cBuyRate;
        this.sellRateCol = cSellRate;
        this.dateCol     = HeaderColumnsMatcher.findExact(row, fmt, DATE);
        this.numberCol   = HeaderColumnsMatcher.findExact(row, fmt, NUMBER);

        List<Integer> lots     = findAll(row, fmt, LOT_VOLUME);
        List<Integer> counters = findAll(row, fmt, CNT_VOLUME);

        this.buyLotCol      = lots.size()     >= 1 ? lots.get(0)     : -1;
        this.sellLotCol     = lots.size()     >= 2 ? lots.get(1)     : -1;
        this.buyCounterCol  = counters.size() >= 1 ? counters.get(0) : -1;
        this.sellCounterCol = counters.size() >= 2 ? counters.get(1) : -1;

        this.valid = dateCol >= 0 && numberCol >= 0;
        return valid;
    }

    @Override
    public void invalidate() {
        dateCol = numberCol = buyRateCol = sellRateCol = -1;
        buyLotCol = sellLotCol = buyCounterCol = sellCounterCol = -1;
        valid = false;
    }

    @Override
    public boolean isValid() { return valid; }

    @Override
    public CurrencyTrade toTrade(Row row, DataFormatter fmt, SecurityRef security) {
        if (!valid) return null;

        String date = CellReader.read(row, dateCol, fmt);
        if (!date.matches("\\d{2}\\.\\d{2}\\.\\d{2}")) return null;

        String number = CellReader.read(row, numberCol, fmt);
        if (number.isBlank()) return null;

        return new CurrencyTrade(
                security.ticker(),          // instrument (EURRUB_TOM)
                security.name(),            // lotCurrency (EUR) — см. CurrencyRefParser
                "",                         // counterCurrency (Рубль) — заполним ниже, если нужно
                date,
                number,
                CellReader.read(row, buyRateCol, fmt),
                CellReader.read(row, buyLotCol, fmt),
                CellReader.read(row, buyCounterCol, fmt),
                CellReader.read(row, sellRateCol, fmt),
                CellReader.read(row, sellLotCol, fmt),
                CellReader.read(row, sellCounterCol, fmt)
        );
    }

    private static List<Integer> findAll(Row row, DataFormatter fmt, String name) {
        List<Integer> result = new ArrayList<>();
        for (Cell c : row) {
            if (fmt.formatCellValue(c).trim().equalsIgnoreCase(name)) {
                result.add(c.getColumnIndex());
            }
        }
        return result;
    }
}