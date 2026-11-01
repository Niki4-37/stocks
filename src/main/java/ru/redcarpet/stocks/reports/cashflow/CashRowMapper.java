package ru.redcarpet.stocks.reports.cashflow;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.reports.common.DateParser;
import ru.redcarpet.stocks.reports.common.DecimalParser;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;
import ru.redcarpet.stocks.reports.dto.TotalsEntry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public final class CashRowMapper {

    /** Детальная строка раздела 1.1 или null, если не подходит. */
    public List<CashFlowRow> toCashOperations(Row row, DataFormatter fmt, ColumnHeaders columnHeaders, Currency currency) {
        if (!columnHeaders.hasDate()) return List.of();

        String date = columnHeaders.date(row, fmt);
        if (!DateParser.isBrokerDate(date)) return List.of();

        String op = columnHeaders.operation(row, fmt);
        Optional<CashOperationType> typeOpt = CashOperationType.byDisplayName(op);
        if (typeOpt.isEmpty()) return List.of();
        CashOperationType type = typeOpt.get();

        String incomeStr  = columnHeaders.income(row, fmt);
        String expenseStr = columnHeaders.expense(row, fmt);
        BigDecimal income  = DecimalParser.parse(incomeStr);
        BigDecimal expense = DecimalParser.parse(expenseStr);

        String platform = columnHeaders.platform(row, fmt);

        DividendNote dividendNote = type.isDividend()
                ? DividendNote.parse(columnHeaders.note(row, fmt))
                : null;

        List<CashFlowRow> result = new ArrayList<>(2);

        for (var candidate : List.of(
                Map.entry(CashFlowDirection.INCOME, income),
                Map.entry(CashFlowDirection.EXPENSE, expense))) {
            CashFlowDirection direction = candidate.getKey();
            BigDecimal amount = candidate.getValue();

            if (!isPositive(amount)) continue;
            if (type.getDirection() != null && type.getDirection() != direction) {
                continue;
            }

            result.add(new CashFlowRow(date, op, direction, currency, platform, dividendNote, amount));
        }

        return result;
    }

    /** Итоговая строка 1.3 или null, если не подходит. */
    public TotalsEntry toTotalsEntry(Row row, DataFormatter fmt, ColumnHeaders columnHeaders) {
        String op = columnHeaders.operation(row, fmt);
        Optional<CashOperationType> typeOpt = CashOperationType.byDisplayName(op);
        if (typeOpt.isEmpty() || typeOpt.get().getDirection() != CashFlowDirection.EXPENSE) {
            return null;
        }

        String amount = columnHeaders.expense(row, fmt);
        if (amount.isBlank()) return null;

        String platform = columnHeaders.platform(row, fmt);
        if (platform.isBlank()) return null;

        return new TotalsEntry(op, platform, amount);
    }

    private static boolean hasAnyAmount(BigDecimal income, BigDecimal expense) {
        return isPositive(income) || isPositive(expense);
    }

    private static boolean isPositive(BigDecimal v) {
        return Objects.nonNull(v) && v.compareTo(BigDecimal.ZERO) > 0;
    }

    private static CashFlowDirection directionOf(BigDecimal expense) {
        return isPositive(expense)
                ? CashFlowDirection.EXPENSE
                : CashFlowDirection.INCOME;
    }


}