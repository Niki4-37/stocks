package ru.redcarpet.stocks.reports.utils;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.enums.TableSection;

import java.util.Objects;

/** Определяет, к какому разделу отчёта относится строка по её тексту. */
public final class ReportSectionClassifier {

    private TableSection current = TableSection.BLANK;

    public TableSection classify(Row row, DataFormatter fmt) {
        if (Objects.isNull(row) || Objects.isNull(fmt)) return current;
        TableSection next = TableSection.byMarker(getSectionMarker(rowText(row, fmt)));
        if (Objects.nonNull(next)) current = next;
        return current; // <-- всегда возвращаем текущую секцию, даже если строка не маркер
    }

    // получение маркера из строки
    private static String getSectionMarker(String text) {
        if (text.isEmpty()) return text;
        int i = 0;
        while (isDigitsAndDots(text, i)) i++;
        return i == 0 ? Strings.EMPTY : cutTailDot(text.substring(0, i));
    }

    private static boolean isDigitsAndDots(String text, int charNum) {
        return charNum < text.length() && (Character.isDigit(text.charAt(charNum)) || text.charAt(charNum) == '.');
    }

    private static String cutTailDot(String raw) {
        while (raw.endsWith(".")) raw = raw.substring(0, raw.length() - 1);
        return raw;
    }

    // получение текста из всей строки
    private static String rowText(Row row, DataFormatter fmt) {
        StringBuilder sb = new StringBuilder();
        for (Cell c : row) sb.append(fmt.formatCellValue(c)).append(' ');
        return sb.toString().trim();
    }
}