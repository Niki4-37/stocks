package ru.redcarpet.stocks.reports.dto;

import ru.redcarpet.stocks.reports.cashflow.DividendNote;

/**
 * Движение денежных средств по разделу 1.1.
 * @param date        дата операции
 * @param operation   операция (Дивиденды, Урегулирование сделок, …)
 * @param direction   INCOME / EXPENSE (приход/расход)
 * @param amount      сумма (положительная, как в отчёте)
 * @param platform    площадка (ММВБ, СПБ, МосБирж(ВР/ДМ), …)
 * @param note        «Примечание» (только для Дивидендов — эмитент/ISIN/налог)
 * @param dividendNote подробно о дивиденде
 * @param income      сумма зачисления (0, если расход)
 * @param expense     сумма списания (0, если приход)
 */
public record CashFlowRow(
        String date,
        String operation,
        Direction direction,
        String amount,
        String platform,
        String note,
        DividendNote dividendNote,
        String income,
        String expense
) {
 public enum Direction { INCOME, EXPENSE }
}
