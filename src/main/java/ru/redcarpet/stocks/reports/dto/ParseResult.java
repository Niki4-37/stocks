package ru.redcarpet.stocks.reports.dto;

import ru.redcarpet.stocks.reports.cashflow.CashParseResult;

import java.util.List;

/** Результат обработки отчета
 * @param cashFlows операции зачисления/списания/проценты по займам на счет из раздела 1.1.1
 * @param stocks сделки по ценным бумагам из раздела 2.1
 * @param funds сделки с фондами из раздела 2.1
 * @param currencies сделки с валютой из раздела 2.1
 * @param checks сверка по разсделу 1.3 и по операциям из 1.1.1
*/
public record ParseResult(
        CashParseResult cashFlows,
        List<StockTrade> stocks,
        List<FundTrade> funds,
        List<BondTrade> bonds,
        List<CurrencyTrade> currencies,
        List<CheckRow> checks
) { }
