package ru.redcarpet.stocks.reports.common;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;
import java.util.Objects;

public final class RowValues {

    private RowValues() {}

    public static String firstNonBlank(Row row, DataFormatter fmt) {
        if (Objects.isNull(row)) return "";
        for (Cell cell : row) {
            String value = CellReader.read(cell, fmt);
            if (!value.isEmpty()) return value;
        }
        return "";
    }

    public static String valueAfterLabel(Row row, DataFormatter fmt, String label) {
        if (Objects.isNull(row) || Objects.isNull(label)) return "";
        boolean found = false;
        for (Cell cell : row) {
            String value = CellReader.read(cell, fmt);
            if (found) {
                if (!value.isEmpty()) return value;
                continue;
            }
            if (value.equalsIgnoreCase(label)) found = true;
        }
        return "";
    }

    public static String nextNonBlank(List<String> cells, int from) {
        if (Objects.isNull(cells) || from < 0) return "";
        for (int i = from; i < cells.size(); i++) {
            if (!cells.get(i).isEmpty()) return cells.get(i);
        }
        return "";
    }
}