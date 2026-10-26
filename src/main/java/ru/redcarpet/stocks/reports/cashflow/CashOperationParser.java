package ru.redcarpet.stocks.reports.cashflow;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Раздел 1.1 — операции с наличными (списания/зачисления). */
@Component
public final class CashOperationParser {

    public List<CashFlowRow> parse(List<Row> rows, DataFormatter fmt) {
        List<CashFlowRow> result = new ArrayList<>();
        ColumnHeaders columns = new ColumnHeaders();
        CahsOperationRowMapper mapper = new CahsOperationRowMapper(columns);

        for (Row row : rows) {
            columns.tryUpdateCashFlow(row, fmt);
            if (!columns.isValid()) continue;

            CashFlowRow op = mapper.toRubCashOperation(row, fmt);
            if (Objects.nonNull(op)) result.add(op);
        }
        return result;
    }
}