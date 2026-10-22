package ru.redcarpet.stocks.reports.utils;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.dto.OperationRow;
import ru.redcarpet.stocks.reports.dto.TotalsEntry;

import java.util.regex.Pattern;

public final class RowMapper {

    private static final Pattern DATE_PATTERN =
            Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{2}");

    private final ColumnLayout layout;

    public RowMapper(ColumnLayout layout) {
        this.layout = layout;
    }

    /** Детальная строка раздела 1.1 или null, если не подходит. */
    public OperationRow toDetail(Row row, DataFormatter fmt) {
        if (!layout.isValid() || !layout.hasDate()) return null;

        String date = layout.date(row, fmt);
        if (!DATE_PATTERN.matcher(date).matches()) return null;

        String op = layout.operation(row, fmt);
        if (!TargetOperations.contains(op)) return null;

        String amount = layout.amount(row, fmt);
        if (amount.isBlank()) return null;

        String platform = layout.platform(row, fmt);
        if (platform.isBlank()) return null;

        return new OperationRow(date, op, amount, platform);
    }

    /** Итоговая строка 1.3 или null, если не подходит. */
    public TotalsEntry toTotalsEntry(Row row, DataFormatter fmt) {
        if (!layout.isValid()) return null;

        String op = layout.operation(row, fmt);
        if (!TargetOperations.contains(op)) return null;

        String amount = layout.amount(row, fmt);
        if (amount.isBlank()) return null;

        String platform = layout.platform(row, fmt);
        if (platform.isBlank()) return null;

        return new TotalsEntry(op, platform, amount);
    }
}