package ru.redcarpet.stocks.reports.trades;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.common.CellReader;
import ru.redcarpet.stocks.reports.common.RowValues;
import ru.redcarpet.stocks.reports.dto.SecurityIdentityFromReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/** Извлекает SecurityIdentityFromReport из строки-идентификатора бумаги в разделе 2.1. */
public final class SecurityIdentityParser {

    private static final Pattern ISIN_PATTERN =
            Pattern.compile("^[A-Z]{2}[A-Z0-9]{9}[0-9]$");

    private SecurityIdentityParser() {}

    public static SecurityIdentityFromReport tryParse(Row row, DataFormatter fmt) {
        if (Objects.isNull(row)) return null;

        String ticker = RowValues.firstNonBlank(row, fmt);
        if (ticker.isBlank()) return null;

        List<String> cells = CellReader.readAll(row, fmt);

        // индекс последней ячейки, похожей на ISIN
        int isinIdx = lastIsinIndex(cells);
        if (isinIdx < 0) return null;

        String isin = cells.get(isinIdx);
        String name = RowValues.nextNonBlank(cells, isinIdx + 1);

        return new SecurityIdentityFromReport(ticker, isin, name);
    }

    /** Индекс последней ячейки, значение которой похоже на ISIN; -1, если нет. */
    private static int lastIsinIndex(List<String> cells) {
        for (int i = cells.size() - 1; i >= 0; i--) {
            if (ISIN_PATTERN.matcher(cells.get(i)).matches()) return i;
        }
        return -1;
    }
}