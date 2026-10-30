package ru.redcarpet.stocks.reports.common;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class CellReader {

    private static final String DATE_FORMAT = "dd.MM.yy";

    private CellReader() {}

    /** Значение ячейки: дата — "dd.MM.yy", иначе formatCellValue().trim(). */
    public static String read(Cell cell, DataFormatter fmt) {
        if (Objects.isNull(cell) || Objects.isNull(fmt)) return Strings.EMPTY;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return new SimpleDateFormat(DATE_FORMAT).format(cell.getDateCellValue());
        }
        return fmt.formatCellValue(cell).trim();
    }

    /** Значение ячейки по индексу столбца. */
    public static String read(Row row, int colIdx, DataFormatter fmt) {
        if (colIdx < 0) return Strings.EMPTY;
        return read(row.getCell(colIdx), fmt);
    }

    /** Все значения строки, в порядке ячеек. */
    public static List<String> readAll(Row row, DataFormatter fmt) {
        if (Objects.isNull(row) || Objects.isNull(fmt)) return List.of();
        List<String> result = new ArrayList<>();
        for (Cell cell : row) {
            result.add(read(cell, fmt));
        }
        return result;
    }
}