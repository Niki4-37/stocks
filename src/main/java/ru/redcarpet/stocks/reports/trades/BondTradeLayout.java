package ru.redcarpet.stocks.reports.trades;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.common.CellReader;
import ru.redcarpet.stocks.reports.common.HeaderColumnsMatcher;
import ru.redcarpet.stocks.reports.dto.BondTrade;
import ru.redcarpet.stocks.reports.dto.SecurityRef;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;

import java.util.ArrayList;
import java.util.List;

public final class BondTradeLayout implements TradeLayout<BondTrade> {

    private static final String DATE       = "Дата";
    private static final String NUMBER     = "Номер";
    private static final String BOUGHT     = "Куплено, шт";
    private static final String SOLD       = "Продано, шт";
    private static final String PRICE      = "Цена, %";
    private static final String PAYMENT    = "Сумма платежа";
    private static final String PROCEEDS   = "Сумма выручки";
    private static final String BUY_ACCR   = "НКД Покупки";
    private static final String SELL_ACCR  = "НКД Продажи";
    private static final String CURRENCY   = "Валюта";
    private static final String DELIVERY   = "Поставка (факт)";
    private static final String PLACE      = "Место сделки";

    private int dateCol = -1;
    private int numberCol = -1;
    private int boughtQtyCol = -1;
    private int soldQtyCol = -1;
    private int buyPriceCol = -1;
    private int sellPriceCol = -1;
    private int paymentCol = -1;
    private int proceedsCol = -1;
    private int buyAccruedCol = -1;
    private int sellAccruedCol = -1;
    private int currencyCol = -1;
    private int deliveryCol = -1;
    private int placeCol = -1;
    private boolean valid;

    @Override
    public boolean isSupports(TradeSubsection subsection) {
        return subsection == TradeSubsection.BOND;
    }

    @Override
    public boolean tryUpdate(Row row, DataFormatter fmt) {
        int cBought = HeaderColumnsMatcher.findExact(row, fmt, BOUGHT);
        int cSold   = HeaderColumnsMatcher.findExact(row, fmt, SOLD);
        if (cBought < 0 && cSold < 0) return false;

        // защита от шапки акции/пая: у неё "Цена", а не "Цена, %"
        int cPricePct = HeaderColumnsMatcher.findExact(row, fmt, PRICE);
        if (cPricePct < 0) return false;

        this.boughtQtyCol = cBought;
        this.soldQtyCol   = cSold;
        this.dateCol      = HeaderColumnsMatcher.findExact(row, fmt, DATE);
        this.numberCol    = HeaderColumnsMatcher.findExact(row, fmt, NUMBER);
        this.paymentCol   = HeaderColumnsMatcher.findExact(row, fmt, PAYMENT);
        this.proceedsCol  = HeaderColumnsMatcher.findExact(row, fmt, PROCEEDS);
        this.buyAccruedCol = HeaderColumnsMatcher.findExact(row, fmt, BUY_ACCR);
        this.sellAccruedCol = HeaderColumnsMatcher.findExact(row, fmt, SELL_ACCR);
        this.currencyCol  = HeaderColumnsMatcher.findExact(row, fmt, CURRENCY);
        this.deliveryCol  = HeaderColumnsMatcher.findExact(row, fmt, DELIVERY);
        this.placeCol     = HeaderColumnsMatcher.findExact(row, fmt, PLACE);

        // две "Цена, %" — первая покупка, вторая продажа
        List<Integer> priceCols = findAll(row, fmt, PRICE);
        this.buyPriceCol  = priceCols.size() >= 1 ? priceCols.get(0) : -1;
        this.sellPriceCol = priceCols.size() >= 2 ? priceCols.get(1) : -1;

        this.valid = dateCol >= 0 && numberCol >= 0;
        return this.valid;
    }

    @Override
    public void invalidate() {
        dateCol = numberCol = boughtQtyCol = soldQtyCol = -1;
        buyPriceCol = sellPriceCol = paymentCol = proceedsCol = -1;
        buyAccruedCol = sellAccruedCol = currencyCol = deliveryCol = placeCol = -1;
        valid = false;
    }

    @Override
    public boolean isValid() { return valid; }

    @Override
    public BondTrade toTrade(Row row, DataFormatter fmt, SecurityRef security) {
        if (!valid) return null;

        String date = CellReader.read(row, dateCol, fmt);
        if (!date.matches("\\d{2}\\.\\d{2}\\.\\d{2}")) return null;

        String number = CellReader.read(row, numberCol, fmt);
        if (number.isBlank()) return null;

        return new BondTrade(
                security.ticker(),
                security.isin(),
                security.name(),
                date,
                number,
                CellReader.read(row, boughtQtyCol, fmt),
                CellReader.read(row, buyPriceCol, fmt),
                CellReader.read(row, paymentCol, fmt),
                CellReader.read(row, buyAccruedCol, fmt),
                CellReader.read(row, soldQtyCol, fmt),
                CellReader.read(row, sellPriceCol, fmt),
                CellReader.read(row, proceedsCol, fmt),
                CellReader.read(row, sellAccruedCol, fmt),
                CellReader.read(row, currencyCol, fmt),
                CellReader.read(row, deliveryCol, fmt),
                CellReader.read(row, placeCol, fmt)
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