package ru.redcarpet.stocks.reports.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;
import java.util.Objects;

/** Поиск индексов столбцов в строке-шапке по списку синонимов. */
public final class HeaderColumnsMatcher {

    private HeaderColumnsMatcher() {}

    /** Первый совпавший синоним; -1 если ни один не найден. */
    public static int findFirst(Row row, DataFormatter fmt, List<String> names) {
        if (Objects.isNull(row) || Objects.isNull(fmt) || Objects.isNull(names) || names.isEmpty()) return -1;
        for (String name : names) {
            int idx = findExact(row, fmt, name);
            if (idx >= 0) return idx;
        }
        return -1;
    }

    /** Точное совпадение (без учёта регистра, с trim). -1 если нет. */
    private static int findExact(Row row, DataFormatter fmt, String name) {
        for (Cell c : row) {
            if (fmt.formatCellValue(c).trim().equalsIgnoreCase(name)) {
                return c.getColumnIndex();
            }
        }
        return -1;
    }
}