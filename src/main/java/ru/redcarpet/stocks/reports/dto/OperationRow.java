package ru.redcarpet.stocks.reports.dto;

public record OperationRow(
        String date,
        String operation,
        String amount,
        String platform
) {
}
