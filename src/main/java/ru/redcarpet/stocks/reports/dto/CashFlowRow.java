package ru.redcarpet.stocks.reports.dto;

import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.reports.cashflow.DividendNote;

import java.util.Currency;

/**
 * Движение денежных средств по разделу 1.1.
 * @param date        дата операции
 * @param operation   операция (Дивиденды, Урегулирование сделок, …)
 * @param direction   INCOME / EXPENSE (приход/расход)
 * @param platform    площадка (ММВБ, СПБ, МосБирж(ВР/ДМ), …)
 * @param dividendNote подробно о дивиденде
 * @param income      сумма зачисления (0, если расход)
 * @param expense     сумма списания (0, если приход)
 */
public record CashFlowRow(
        String date,
        String operation,
        CashFlowDirection direction,
        Currency currency,
        String platform,
        DividendNote dividendNote,
        String income,
        String expense
) {
    public String amount() {
        return direction == CashFlowDirection.EXPENSE ? expense : income;
    }

    public String noteRaw() {
        return dividendNote == null ? "" : dividendNote.raw();
    }
}
