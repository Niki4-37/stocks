package ru.redcarpet.stocks.reports.dto;

import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.reports.cashflow.DividendNote;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Движение денежных средств по разделу 1.1.
 * @param date        дата операции
 * @param operation   операция (Дивиденды, Урегулирование сделок, …)
 * @param direction   INCOME / EXPENSE (приход/расход)
 * @param platform    площадка (ММВБ, СПБ, МосБирж(ВР/ДМ), …)
 * @param dividendNote подробно о дивиденде
 * @param amount      сумма зачисления/списания
 */
public record CashFlowRow(
        String date,
        CashOperationType operation,
        CashFlowDirection direction,
        Currency currency,
        String platform,
        DividendNote dividendNote,
        BigDecimal amount
) {
    public boolean isIncome()  { return direction == CashFlowDirection.INCOME; }

    public String noteRaw() {
        return dividendNote == null ? "" : dividendNote.raw();
    }
}
