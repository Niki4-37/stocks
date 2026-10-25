package ru.redcarpet.stocks.reports.trades;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.dto.SecurityRef;

/** Извлекает SecurityRef из строки-идентификатора бумаги в разделе 2.1. */
public final class SecurityRefParser {

    private static final String ISIN_LABEL = "ISIN:";
    private static final String REG_LABEL  = "Номер рег.:";

    private SecurityRefParser() {}

    public static SecurityRef tryParse(Row row, DataFormatter fmt) {
        if (row == null) return null;

        String ticker = firstNonBlank(row, fmt);
        if (ticker.isBlank()) return null;

        String isin      = valueAfterLabel(row, fmt, ISIN_LABEL);
        String regNumber = valueAfterLabel(row, fmt, REG_LABEL);
        if (isin.isBlank() && regNumber.isBlank()) return null;

        // Название — следующая непустая ячейка ПОСЛЕ значения ISIN
        // (если ISIN пуст — после значения "Номер рег.")
        String anchorValue = !isin.isBlank() ? isin : regNumber;
        String name = nextNonBlankAfterValue(row, fmt, anchorValue, ticker);

        return new SecurityRef(ticker, isin, regNumber, name);
    }

    private static String firstNonBlank(Row row, DataFormatter fmt) {
        for (Cell c : row) {
            String v = fmt.formatCellValue(c).trim();
            if (!v.isEmpty()) return v;
        }
        return "";
    }

    /** Первая непустая ячейка после метки, но не другая метка. */
    private static String valueAfterLabel(Row row, DataFormatter fmt, String label) {
        boolean found = false;
        for (Cell c : row) {
            String v = fmt.formatCellValue(c).trim();
            if (!found) {
                if (v.equalsIgnoreCase(label)) found = true;
                continue;
            }
            if (v.isEmpty()) continue;
            if (v.equalsIgnoreCase(ISIN_LABEL) || v.equalsIgnoreCase(REG_LABEL)) {
                return "";
            }
            return v;
        }
        return "";
    }

    /** Первая непустая ячейка после значения {@code anchorValue}, кроме {@code skip}. */
    private static String nextNonBlankAfterValue(Row row, DataFormatter fmt,
                                                 String anchorValue, String skip) {
        boolean foundAnchor = false;
        for (Cell c : row) {
            String v = fmt.formatCellValue(c).trim();
            if (!foundAnchor) {
                if (!v.isEmpty() && v.equals(anchorValue)) foundAnchor = true;
                continue;
            }
            if (v.isEmpty()) continue;
            if (v.equals(skip)) continue;
            return v;
        }
        return "";
    }
}