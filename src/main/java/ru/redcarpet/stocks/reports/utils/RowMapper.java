package ru.redcarpet.stocks.reports.utils;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.dto.OperationRow;
import ru.redcarpet.stocks.reports.dto.TotalsEntry;

import java.util.regex.Pattern;

public final class RowMapper {

    private static final Pattern DATE_PATTERN =
            Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{2}");

    private final ColumnHeaders columnHeaders;

    public RowMapper(ColumnHeaders columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    /** Детальная строка раздела 1.1 или null, если не подходит. */
    public OperationRow toRubCashOperation(Row row, DataFormatter fmt) {
        if (!columnHeaders.isValid() || !columnHeaders.hasDate()) return null;

        String date = columnHeaders.date(row, fmt);
        if (!DATE_PATTERN.matcher(date).matches()) return null;

        String op = columnHeaders.operation(row, fmt);
        if (!RowOperationNames.contains(op)) return null;

        String amount = columnHeaders.amount(row, fmt);
        if (amount.isBlank()) return null;

        String platform = columnHeaders.platform(row, fmt);
        if (platform.isBlank()) return null;

        return new OperationRow(date, op, amount, platform);
    }

    /** Итоговая строка 1.3 или null, если не подходит. */
    public TotalsEntry toTotalsEntry(Row row, DataFormatter fmt) {
        if (!columnHeaders.isValid()) return null;

        String op = columnHeaders.operation(row, fmt);
        if (!RowOperationNames.contains(op)) return null;

        String amount = columnHeaders.amount(row, fmt);
        if (amount.isBlank()) return null;

        String platform = columnHeaders.platform(row, fmt);
        if (platform.isBlank()) return null;

        return new TotalsEntry(op, platform, amount);
    }
}