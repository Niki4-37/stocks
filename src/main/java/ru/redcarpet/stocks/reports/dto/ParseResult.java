package ru.redcarpet.stocks.reports.dto;

import java.util.List;

/** Результат обработки отчета
 * @param cashFlows операции со средствами на счете из раздела 1.1.1
 * @param stocks сделки по ценным бумагам из раздела 2.1
 * @param funds сделки с фондами из раздела 2.1
 * @param currencies сделки с валютой из раздела 2.1
 * @param checks сверка по разсделу 1.3 и по операциям из 1.1.1
*/
public record ParseResult(
        List<CashFlowRow> cashFlows,
        List<StockTrade> stocks,
        List<FundTrade> funds,
        List<BondTrade> bonds,
        List<CurrencyTrade> currencies,
        List<CheckRow> checks
) {
}
