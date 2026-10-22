package ru.redcarpet.stocks.reports.utils;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.text.SimpleDateFormat;
import java.util.Objects;

/** Чтение значения ячейки по индексу с учётом формата. */
public final class CellReader {

    private static final String DATE_FORMAT = "dd.MM.yy";

    private CellReader() {}

    public static String read(Row row, int colIdx, DataFormatter fmt) {
        if (Objects.isNull(row) || Objects.isNull(fmt) || colIdx < 0) return Strings.EMPTY;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return Strings.EMPTY;

        if (cell.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {
            return new SimpleDateFormat(DATE_FORMAT).format(cell.getDateCellValue());
        }
        return fmt.formatCellValue(cell).trim();
    }
}