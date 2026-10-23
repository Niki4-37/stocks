package ru.redcarpet.stocks.reports.dto;

/**
 * Операции из раздела 1.1.1 (движение наличности).
 * @param date дата операции
 * @param operation наименование операции из утилитного RowOperationNames
 * @param amount сумма операции
 * @param platform площадка, на которой выполняется операция
 */
 public record OperationRow(
        String date,
        String operation,
        String amount,
        String platform
) {
}
