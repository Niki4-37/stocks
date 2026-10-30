package ru.redcarpet.stocks.reports.common;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Поиск индексов столбцов в строке-шапке по списку синонимов. */
public final class HeaderColumnsMatcher {

    private HeaderColumnsMatcher() {}

    /** Индекс столбца первого совпавшего синонима; -1 если ни один не найден. */
    public static int findFirstIndexExact(Row row, DataFormatter fmt, List<String> names) {
        if (Objects.isNull(row) || Objects.isNull(fmt) || Objects.isNull(names) || names.isEmpty()) return -1;
        for (String name : names) {
            int idx = findIndexExact(row, fmt, name);
            if (idx >= 0) return idx;
        }
        return -1;
    }

    /** Первый совпавший синоним по префиксу; -1 если ни один не найден. */
    public static int findFirstIndexStartsWith(Row row, DataFormatter fmt, List<String> names) {
        if (Objects.isNull(row) || Objects.isNull(fmt) || Objects.isNull(names) || names.isEmpty()) return -1;
        for (String name : names) {
            int idx = findIndexStartsWith(row, fmt, name);
            if (idx >= 0) return idx;
        }
        return -1;
    }

    /** Индекс столбца точного совпадения (без учёта регистра, с trim). -1 если нет. */
    public static int findIndexExact(Row row, DataFormatter fmt, String name) {
        if (Objects.isNull(row) || Objects.isNull(fmt) || Objects.isNull(name) || name.isEmpty()) return -1;
        for (Cell cell : row) {
            if (fmt.formatCellValue(cell).trim().equalsIgnoreCase(name)) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    /** Все индексы столбцов, где значение ячейки точно совпадает с {@code name}. */
    public static List<Integer> findAllIndexesExact(Row row, DataFormatter fmt, String name) {
        if (Objects.isNull(row) || Objects.isNull(fmt) || Objects.isNull(name) || name.isEmpty()) return List.of();
        List<Integer> result = new ArrayList<>();
        for (Cell cell : row) {
            if (fmt.formatCellValue(cell).trim().equalsIgnoreCase(name)) {
                result.add(cell.getColumnIndex());
            }
        }
        return result;
    }

    /** Все индексы столбцов для каждого из синонимов (объединяет, в порядке ячеек). */
    public static List<Integer> findAllIndexesExactByCellOrder(Row row, DataFormatter fmt, List<String> names) {
        if (Objects.isNull(row) || Objects.isNull(fmt) || Objects.isNull(names) || names.isEmpty()) return List.of();
        List<Integer> result = new ArrayList<>();
        for (Cell cell : row) {
            String cellValue = fmt.formatCellValue(cell).trim();
            for (String name : names) {
                if (cellValue.equalsIgnoreCase(name)) {
                    result.add(cell.getColumnIndex());
                    break;
                }
            }
        }
        return result;
    }

    /** Первая ячейка, значение которой начинается с {@code prefix} (без учёта регистра). */
    public static int findIndexStartsWith(Row row, DataFormatter fmt, String prefix) {
        if (Objects.isNull(row) || Objects.isNull(fmt) || Objects.isNull(prefix) || prefix.isEmpty()) return -1;
        String target = prefix.trim().toLowerCase();
        for (Cell cell : row) {
            String callValue = fmt.formatCellValue(cell).trim().toLowerCase();
            if (callValue.startsWith(target)) return cell.getColumnIndex();
        }
        return -1;
    }

    /** Все индексы, где значение ячейки начинается с {@code prefix}. */
    public static List<Integer> findAllIndexesStartsWith(Row row, DataFormatter fmt, String prefix) {
        if (Objects.isNull(row) || Objects.isNull(fmt) || Objects.isNull(prefix) || prefix.isEmpty()) return List.of();
        String target = prefix.trim().toLowerCase();
        List<Integer> result = new ArrayList<>();
        for (Cell cell : row) {
            String cellValue = fmt.formatCellValue(cell).trim().toLowerCase();
            if (cellValue.startsWith(target)) result.add(cell.getColumnIndex());
        }
        return result;
    }
}