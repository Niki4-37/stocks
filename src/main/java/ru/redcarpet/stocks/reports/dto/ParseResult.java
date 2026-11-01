package ru.redcarpet.stocks.reports.dto;

import ru.redcarpet.stocks.reports.cashflow.CashParseResult;

import java.util.List;

/** Результат обработки отчета
 * @param cashFlows операции зачисления/списания/проценты по займам на счет из раздела 1.1.1
 * @param trades сделки по ценным бумагам/с валютой из раздела 2.1
 * @param checks сверка по разсделу 1.3 и по операциям из 1.1.1
*/
public record ParseResult(
        CashParseResult cashFlows,
        TradesResult trades,
        List<CurrencyTrade> currencies,
        List<CheckRow> checks
) { }
