package ru.redcarpet.stocks.reports.dto;

import java.util.List;

public record ParseResult(
        List<OperationRow> rows,
        List<CheckRow> checks // сверка с разделом 1.3
) {
}
