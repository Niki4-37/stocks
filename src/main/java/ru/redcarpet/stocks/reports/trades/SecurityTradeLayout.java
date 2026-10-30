package ru.redcarpet.stocks.reports.trades;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.dto.SecurityIdentityFromReport;
import ru.redcarpet.stocks.reports.common.CellReader;
import ru.redcarpet.stocks.reports.common.HeaderColumnsMatcher;

import java.util.List;

import static ru.redcarpet.stocks.reports.common.HeaderColumnsMatcher.findAllIndexesStartsWith;

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
    private static final String PLACE      = "Место сделки";

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
    private int placeCol = -1;
    protected boolean valid;

    @Override
    public boolean tryUpdate(Row row, DataFormatter fmt) {
        int cBought = HeaderColumnsMatcher.findIndexExact(row, fmt, BOUGHT);
        int cSold   = HeaderColumnsMatcher.findIndexExact(row, fmt, SOLD);
        if (cBought < 0 && cSold < 0) return false;

        this.boughtQtyCol = cBought;
        this.soldQtyCol   = cSold;
        this.dateCol      = HeaderColumnsMatcher.findIndexExact(row, fmt, DATE);
        this.numberCol    = HeaderColumnsMatcher.findIndexExact(row, fmt, NUMBER);
        this.paymentCol   = HeaderColumnsMatcher.findIndexExact(row, fmt, PAYMENT);
        this.proceedsCol  = HeaderColumnsMatcher.findIndexExact(row, fmt, PROCEEDS);
        this.currencyCol  = HeaderColumnsMatcher.findIndexExact(row, fmt, CURRENCY);
        this.deliveryCol  = HeaderColumnsMatcher.findIndexExact(row, fmt, DELIVERY);
        this.placeCol     = HeaderColumnsMatcher.findIndexExact(row, fmt, PLACE);

        List<Integer> priceCols = findAllIndexesStartsWith(row, fmt, PRICE);
        this.buyPriceCol  = priceCols.size() >= 1 ? priceCols.get(0) : -1;
        this.sellPriceCol = priceCols.size() >= 2 ? priceCols.get(1) : -1;

        this.valid = dateCol >= 0 && numberCol >= 0;
        return this.valid;
    }

    @Override
    public void invalidate() {
        dateCol = numberCol = boughtQtyCol = soldQtyCol = -1;
        paymentCol = proceedsCol = currencyCol = deliveryCol = -1;
        buyPriceCol = sellPriceCol = placeCol = -1;
        valid = false;
    }

    @Override
    public boolean isValid() { return valid; }

    @Override
    public T toTrade(Row row, DataFormatter fmt, SecurityIdentityFromReport security) {
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
                CellReader.read(row, deliveryCol, fmt),
                CellReader.read(row, placeCol, fmt)
        );
    }

    /** Собирает конкретный DTO из уже прочитанных значений. */
    protected abstract T build(SecurityIdentityFromReport security,
                               String date, String number,
                               String boughtQty, String buyPrice, String paymentAmount,
                               String soldQty, String sellPrice, String proceeds,
                               String currency, String deliveryFact, String place);

}