package ru.redcarpet.stocks.reports.cashflow;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.reports.dto.TotalsEntry;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Раздел 1.3 — итоговые сборы и штрафы. */
@Component
public final class FeesAndPenaltiesParser {

    public Map<String, BigDecimal> parse(List<Row> rows, DataFormatter fmt) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        ColumnHeaders columns = new ColumnHeaders();
        CahsOperationRowMapper mapper = new CahsOperationRowMapper(columns);

        for (Row row : rows) {
            columns.tryUpdateFees(row, fmt);
            if (!columns.isValid()) continue;

            TotalsEntry entry = mapper.toTotalsEntry(row, fmt);
            if (Objects.isNull(entry)) continue;

            BigDecimal parsedAmount = DecimalParser.parse(entry.amount());
            if (Objects.isNull(parsedAmount)) continue;

            totals.merge(key(entry.operation(), entry.platform()), parsedAmount, BigDecimal::add);
        }
        return totals;
    }

    private static String key(String op, String platform) {
        return op + "|" + platform;
    }
}