package ru.redcarpet.stocks.reports.cashflow;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.common.CellReader;

import static ru.redcarpet.stocks.reports.common.HeaderColumnsMatcher.findFirst;

/**
 * Индексы столбцов текущей таблицы отчёта БКС.
 * Логические поля: дата, операция, сумма списания, площадка.
 */
public final class ColumnHeaders {

    private int dateCol = -1;
    private int operationCol = -1;
    private int incomeCol = -1;
    private int expenseCol = -1;
    private int platformCol = -1;
    private int noteCol = -1;
    private boolean valid;

    /** Шапка 1.1: нужны «Сумма зачисления» ИЛИ «Сумма списания» + «Площадка» + «Операция». */
    public boolean tryUpdateCashFlow(Row row, DataFormatter fmt) {
        int cOp     = findFirst(row, fmt, ColumnNames.OPERATION);
        int cPlat   = findFirst(row, fmt, ColumnNames.PLATFORM);
        int cIncome = findFirst(row, fmt, ColumnNames.INCOME);
        int cExpense= findFirst(row, fmt, ColumnNames.EXPENSE);
        if (cOp < 0 || cPlat < 0 || (cIncome < 0 && cExpense < 0)) return false;

        this.dateCol      = findFirst(row, fmt, ColumnNames.DATE);
        this.operationCol = cOp;
        this.incomeCol    = cIncome;
        this.expenseCol   = cExpense;
        this.platformCol  = cPlat;
        this.noteCol      = findFirst(row, fmt, ColumnNames.NOTE);
        this.valid        = true;
        return true;
    }

    /** Шапка 1.3: «Сумма списания» ИЛИ «Начисленная комиссия» + «Площадка» + «Операция/Вид сбора». */
    public boolean tryUpdateFees(Row row, DataFormatter fmt) {
        int cOp =       findFirst(row, fmt, ColumnNames.OPERATION);
        int cExpense =  findFirst(row, fmt, ColumnNames.EXPENSE);
        int cPlat =     findFirst(row, fmt, ColumnNames.PLATFORM);
        if (cOp < 0 || cExpense < 0 || cPlat < 0) return false;

        this.operationCol = cOp;
        this.expenseCol   = cExpense;
        this.platformCol  = cPlat;
        this.valid        = true;
        return true;
    }

    public boolean isValid() { return valid; }
    public boolean hasDate() { return dateCol >= 0; }

    public String date(Row row, DataFormatter fmt)      { return CellReader.read(row, dateCol, fmt); }
    public String operation(Row row, DataFormatter fmt) { return CellReader.read(row, operationCol, fmt); }
    public String income(Row row, DataFormatter fmt)    { return CellReader.read(row, incomeCol, fmt); }
    public String expense(Row row, DataFormatter fmt)   { return CellReader.read(row, expenseCol, fmt); }
    public String platform(Row row, DataFormatter fmt)  { return CellReader.read(row, platformCol, fmt); }
    public String note(Row row, DataFormatter fmt)      { return CellReader.read(row, noteCol, fmt); }
}