package ru.redcarpet.stocks.reports.cashflow;

import ru.redcarpet.stocks.reports.common.DecimalParser;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record CashParseResult(
        List<CashFlowRow> incomes,
        List<CashFlowRow> expenses,
        List<CashFlowRow> overnightInterests
) {
    /** Итог по овернайт-процентам — для UI. */
    public BigDecimal overnightInterestsTotal() {
        return overnightInterests.stream()
                .map(r -> DecimalParser.parse(r.amount()))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<CashFlowRow> summarizedCash() {
        List<CashFlowRow> result = new ArrayList<>(incomes.size()
                + expenses.size() + overnightInterests.size());
        result.addAll(incomes);
        result.addAll(expenses);
        result.addAll(overnightInterests);
        return result;
    }
}