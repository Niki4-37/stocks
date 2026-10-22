package ru.redcarpet.stocks.reports.utils;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.text.SimpleDateFormat;
import java.util.List;

public final class ColumnLayout {

    private static final List<String> OP_HEADERS = List.of(
            "Операция",
            "Вид сбора/штрафа",
            "Начисленная комиссия"
    );
    private static final List<String> DATE_HEADERS = List.of(
            "Дата"
    );
    private static final List<String> AMOUNT_HEADERS = List.of(
            "Сумма списания",       // детальная 1.1
            "Начисленная комиссия"  // итоговая 1.3 (новый формат)
    );
    private static final List<String> PLATFORM_HEADERS = List.of(
            "Площадка"
    );

    private static String DATE_FORMAT = "dd.MM.yy";

    private int dateCol = -1;
    private int opCol = -1;
    private int amountCol = -1;
    private int platformCol = -1;
    private boolean valid;

    /** Пытается распознать шапку. true — индексы обновлены. */
    public boolean tryUpdate(Row row, DataFormatter fmt) {
        int cAmt  = findFirstCol(row, fmt, AMOUNT_HEADERS);
        int cPlat = findFirstCol(row, fmt, PLATFORM_HEADERS);
        int cOp = findFirstCol(row, fmt, OP_HEADERS);
        if (cAmt < 0 || cPlat < 0 || cOp < 0) return false;

        this.dateCol     = findFirstCol(row, fmt, DATE_HEADERS);
        this.opCol       = cOp;
        this.amountCol   = cAmt;
        this.platformCol = cPlat;
        this.valid       = true;
        return true;
    }

    public boolean isValid() { return valid; }
    public boolean hasDate() { return dateCol >= 0; }

    public String date(Row row, DataFormatter fmt)     { return cell(row, dateCol, fmt); }
    public String operation(Row row, DataFormatter fmt) { return cell(row, opCol, fmt); }
    public String amount(Row row, DataFormatter fmt)   { return cell(row, amountCol, fmt); }
    public String platform(Row row, DataFormatter fmt) { return cell(row, platformCol, fmt); }

    // ---------- helpers ----------

    private static int findCol(Row row, DataFormatter fmt, String name) {
        for (Cell c : row) {
            if (fmt.formatCellValue(c).trim().equalsIgnoreCase(name)) {
                return c.getColumnIndex();
            }
        }
        return -1;
    }

    private static int findFirstCol(Row row, DataFormatter fmt, List<String> names) {
        for (String name : names) {
            int idx = findCol(row, fmt, name);
            if (idx >= 0) return idx;
        }
        return -1;
    }

    private static String cell(Row row, int idx, DataFormatter fmt) {
        if (idx < 0) return Strings.EMPTY;
        Cell cell = row.getCell(idx);
        if (cell == null) return Strings.EMPTY;
        if (cell.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {
            return new SimpleDateFormat(DATE_FORMAT).format(cell.getDateCellValue());
        }
        return fmt.formatCellValue(cell).trim();
    }
}