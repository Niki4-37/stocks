package ru.redcarpet.stocks.reports.cash;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.reports.common.ColumnHeaders;
import ru.redcarpet.stocks.reports.common.RowMapper;
import ru.redcarpet.stocks.reports.dto.OperationRow;

import java.util.ArrayList;
import java.util.List;

/** Раздел 1.1 — рублёвые операции с наличными (списания). */
@Component
public final class CashOperationParser {

    public List<OperationRow> parse(List<Row> rows, DataFormatter fmt) {
        List<OperationRow> result = new ArrayList<>();
        ColumnHeaders columns = new ColumnHeaders();
        RowMapper mapper = new RowMapper(columns);

        for (Row row : rows) {
            columns.update(row, fmt);
            if (!columns.isValid()) continue;

            OperationRow op = mapper.toRubCashOperation(row, fmt);
            if (op != null) result.add(op);
        }
        return result;
    }
}