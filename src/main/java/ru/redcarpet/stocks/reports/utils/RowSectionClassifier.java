package ru.redcarpet.stocks.reports.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.enums.TableSection;

/** Определяет, к какому разделу отчёта относится строка по её тексту. */
public final class RowSectionClassifier {

    private TableSection current = TableSection.BLANK;

    public TableSection classify(Row row, DataFormatter fmt) {
        TableSection next = TableSection.byMarker(leadingMarker(rowText(row, fmt)));
        if (next != null) current = next;
        return current; // <-- всегда возвращаем текущую, даже если строка не маркер
    }

    // получение маркера из строки
    private static String leadingMarker(String text) {
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if ((c >= '0' && c <= '9') || c == '.') i++;
            else break;
        }
        return i == 0 ? null : text.substring(0, i);
    }

    // получение текста из всей строки
    private static String rowText(Row row, DataFormatter fmt) {
        StringBuilder sb = new StringBuilder();
        for (Cell c : row) sb.append(fmt.formatCellValue(c)).append(' ');
        return sb.toString().trim();
    }
}