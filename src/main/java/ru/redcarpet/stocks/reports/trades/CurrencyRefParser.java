package ru.redcarpet.stocks.reports.trades;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.dto.SecurityIdentityFromReport;

import java.util.Objects;

/** Распознаёт строку-идентификатор валютной подтаблицы: EURRUB_TOM | Валюта лота: | EUR | Сопряж. валюта: | Рубль. */
public final class CurrencyRefParser {

    private static final String LOT_LABEL     = "Валюта лота:";
    private static final String COUNTER_LABEL = "Сопряж. валюта:";

    private CurrencyRefParser() {}

    /** @return SecurityIdentityFromReport(ticker=инструмент, name=валюта лота) или null, если не идентификатор. */
    public static SecurityIdentityFromReport tryParse(Row row, DataFormatter fmt) {
        if (Objects.isNull(row)) return null;

        String instrument = firstNonBlank(row, fmt);
        if (instrument.isBlank()) return null;

        String lot = valueAfterLabel(row, fmt, LOT_LABEL);
        String counter = valueAfterLabel(row, fmt, COUNTER_LABEL);
        if (lot.isBlank() && counter.isBlank()) return null;

        // ticker = "EURRUB_TOM", name = "EUR/RUB" (лот/сопряж.)
        String name = (lot.isBlank() ? "?" : lot) + "/" + (counter.isBlank() ? "?" : counter);
        return new SecurityIdentityFromReport(instrument, "no ISIN", "no name");
    }

    private static String firstNonBlank(Row row, DataFormatter fmt) {
        for (Cell c : row) {
            String v = fmt.formatCellValue(c).trim();
            if (!v.isEmpty()) return v;
        }
        return Strings.EMPTY;
    }

    private static String valueAfterLabel(Row row, DataFormatter fmt, String label) {
        boolean found = false;
        for (Cell c : row) {
            String v = fmt.formatCellValue(c).trim();
            if (found) {
                if (!v.isEmpty()) return v;
                continue;
            }
            if (v.equalsIgnoreCase(label)) found = true;
        }
        return Strings.EMPTY;
    }
}