package ru.redcarpet.stocks.reports.trades;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.dto.SecurityRef;
import ru.redcarpet.stocks.reports.common.CellReader;
import ru.redcarpet.stocks.reports.common.HeaderColumnsMatcher;

import java.util.ArrayList;
import java.util.List;

/** Общая шапка для бумажных подразделов: Акция, Пай. */
public abstract class SecurityTradeLayout<T> implements TradeLayout<T> {

    private static final String DATE     = "Дата";
    private static final String NUMBER   = "Номер";
    private static final String BOUGHT   = "Куплено, шт";
    private static final String SOLD     = "Продано, шт";
    private static final String PAYMENT  = "Сумма платежа";
    private static final String PROCEEDS = "Сумма выручки";
    private static final String CURRENCY = "Валюта";
    private static final String DELIVERY = "Поставка (факт)";
    private static final String PRICE    = "Цена";

    protected int dateCol = -1;
    protected int numberCol = -1;
    protected int boughtQtyCol = -1;
    protected int soldQtyCol = -1;
    protected int paymentCol = -1;
    protected int proceedsCol = -1;
    protected int currencyCol = -1;
    protected int deliveryCol = -1;
    protected int buyPriceCol = -1;
    protected int sellPriceCol = -1;
    protected boolean valid;

    @Override
    public boolean tryUpdate(Row row, DataFormatter fmt) {
        int cBought = HeaderColumnsMatcher.findExact(row, fmt, BOUGHT);
        int cSold   = HeaderColumnsMatcher.findExact(row, fmt, SOLD);
        if (cBought < 0 && cSold < 0) return false;

        this.boughtQtyCol = cBought;
        this.soldQtyCol   = cSold;
        this.dateCol      = HeaderColumnsMatcher.findExact(row, fmt, DATE);
        this.numberCol    = HeaderColumnsMatcher.findExact(row, fmt, NUMBER);
        this.paymentCol   = HeaderColumnsMatcher.findExact(row, fmt, PAYMENT);
        this.proceedsCol  = HeaderColumnsMatcher.findExact(row, fmt, PROCEEDS);
        this.currencyCol  = HeaderColumnsMatcher.findExact(row, fmt, CURRENCY);
        this.deliveryCol  = HeaderColumnsMatcher.findExact(row, fmt, DELIVERY);

        List<Integer> priceCols = findAll(row, fmt, PRICE);
        this.buyPriceCol  = priceCols.size() >= 1 ? priceCols.get(0) : -1;
        this.sellPriceCol = priceCols.size() >= 2 ? priceCols.get(1) : -1;

        this.valid = dateCol >= 0 && numberCol >= 0;
        return this.valid;
    }

    @Override
    public void invalidate() {
        dateCol = numberCol = boughtQtyCol = soldQtyCol = -1;
        paymentCol = proceedsCol = currencyCol = deliveryCol = -1;
        buyPriceCol = sellPriceCol = -1;
        valid = false;
    }

    @Override
    public boolean isValid() { return valid; }

    @Override
    public T toTrade(Row row, DataFormatter fmt, SecurityRef security) {
        if (!valid) return null;

        String date = CellReader.read(row, dateCol, fmt);
        if (!date.matches("\\d{2}\\.\\d{2}\\.\\d{2}")) return null;

        String number = CellReader.read(row, numberCol, fmt);
        if (number.isBlank()) return null;

        return build(
                security,
                date,
                number,
                CellReader.read(row, boughtQtyCol, fmt),
                CellReader.read(row, buyPriceCol, fmt),
                CellReader.read(row, paymentCol, fmt),
                CellReader.read(row, soldQtyCol, fmt),
                CellReader.read(row, sellPriceCol, fmt),
                CellReader.read(row, proceedsCol, fmt),
                CellReader.read(row, currencyCol, fmt),
                CellReader.read(row, deliveryCol, fmt)
        );
    }

    /** Собирает конкретный DTO из уже прочитанных значений. */
    protected abstract T build(SecurityRef security,
                               String date, String number,
                               String boughtQty, String buyPrice, String paymentAmount,
                               String soldQty, String sellPrice, String proceeds,
                               String currency, String deliveryFact);

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