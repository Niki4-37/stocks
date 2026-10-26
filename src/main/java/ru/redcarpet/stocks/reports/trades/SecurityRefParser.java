package ru.redcarpet.stocks.reports.trades;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.dto.SecurityRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/** Извлекает SecurityRef из строки-идентификатора бумаги в разделе 2.1. */
public final class SecurityRefParser {

    private static final Pattern ISIN_PATTERN =
            Pattern.compile("^[A-Z]{2}[A-Z0-9]{9}[0-9]$");

    private SecurityRefParser() {}

    public static SecurityRef tryParse(Row row, DataFormatter fmt) {
        if (Objects.isNull(row)) return null;

        String ticker = firstNonBlank(row, fmt);
        if (ticker.isBlank()) return null;

        List<String> cells = readCells(row, fmt);

        // индекс последней ячейки, похожей на ISIN
        int isinIdx = lastIsinIndex(cells);
        String isin = isinIdx >= 0 ? cells.get(isinIdx) : Strings.EMPTY;

        // должно быть хоть что-то: ISIN
        if (isin.isBlank()) return null;

        // Название — первая непустая ячейка ПОСЛЕ значения ISIN.
        // Если ISIN нет — названия нет (по требованию: только после ISIN).
        String name = isinIdx >= 0 ? nextNonBlank(cells, isinIdx + 1) : "";

        return new SecurityRef(ticker, isin, name);
    }

    private static List<String> readCells(Row row, DataFormatter fmt) {
        List<String> result = new ArrayList<>();
        for (Cell c : row) {
            result.add(fmt.formatCellValue(c).trim());
        }
        return result;
    }

    /** Индекс последней ячейки, значение которой похоже на ISIN; -1, если нет. */
    private static int lastIsinIndex(List<String> cells) {
        for (int i = cells.size() - 1; i >= 0; i--) {
            if (ISIN_PATTERN.matcher(cells.get(i)).matches()) return i;
        }
        return -1;
    }

    /** Первая непустая ячейка начиная с позиции {@code from}. */
    private static String nextNonBlank(List<String> cells, int from) {
        for (int i = from; i < cells.size(); i++) {
            String v = cells.get(i);
            if (!v.isEmpty()) return v;
        }
        return Strings.EMPTY;
    }

    private static String firstNonBlank(Row row, DataFormatter fmt) {
        for (Cell c : row) {
            String v = fmt.formatCellValue(c).trim();
            if (!v.isEmpty()) return v;
        }
        return Strings.EMPTY;
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
            if (v.equalsIgnoreCase(label)) return Strings.EMPTY;
            return v;
        }
        return Strings.EMPTY;
    }
}