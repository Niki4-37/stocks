package ru.redcarpet.stocks.reports.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;

import java.util.Objects;

public final class TradeSubsectionClassifier {

    private TradeSubsection current = TradeSubsection.NONE;

    public TradeSubsection classify(Row row, DataFormatter fmt) {
        if (Objects.isNull(row) || Objects.isNull(fmt)) return current;
        String text = rowText(row, fmt);
        if (text.isEmpty()) return current;

        TradeSubsection next = TradeSubsection.byTitle(text);
        if (next != null) current = next;
        return current;
    }

    public TradeSubsection current() { return current; }

    private static String rowText(Row row, DataFormatter fmt) {
        StringBuilder sb = new StringBuilder();
        for (Cell c : row) sb.append(fmt.formatCellValue(c)).append(' ');
        return sb.toString().trim();
    }
}