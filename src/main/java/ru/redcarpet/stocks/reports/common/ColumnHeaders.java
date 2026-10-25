package ru.redcarpet.stocks.reports.common;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import static ru.redcarpet.stocks.reports.common.HeaderColumnsMatcher.findFirst;

/**
 * Индексы столбцов текущей таблицы отчёта БКС.
 * Логические поля: дата, операция, сумма списания, площадка.
 */
public final class ColumnHeaders {

    private int dateCol = -1;
    private int operationCol = -1;
    private int amountCol = -1;
    private int platformCol = -1;
    private boolean valid;

    /**
     * Пытается распознать шапку. Если шапка не распознана — состояние не меняется.
     */
    public void update(Row row, DataFormatter fmt) {
        int cAmt  = findFirst(row, fmt, ColumnNames.AMOUNT);
        int cPlat = findFirst(row, fmt, ColumnNames.PLATFORM);
        int cOp   = findFirst(row, fmt, ColumnNames.OPERATION);
        if (cAmt < 0 || cPlat < 0 || cOp < 0) return;

        this.dateCol      = findFirst(row, fmt, ColumnNames.DATE);
        this.operationCol = cOp;
        this.amountCol    = cAmt;
        this.platformCol  = cPlat;
        this.valid        = true;
    }

    public boolean isValid() { return valid; }
    public boolean hasDate() { return dateCol >= 0; }

    public String date(Row row, DataFormatter fmt)      { return CellReader.read(row, dateCol, fmt); }
    public String operation(Row row, DataFormatter fmt) { return CellReader.read(row, operationCol, fmt); }
    public String amount(Row row, DataFormatter fmt)    { return CellReader.read(row, amountCol, fmt); }
    public String platform(Row row, DataFormatter fmt)  { return CellReader.read(row, platformCol, fmt); }
}