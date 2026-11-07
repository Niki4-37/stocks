package ru.redcarpet.stocks.reports.cashflow;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

        // запоминаем, было ли сторно (знак в отчёте не совпадал с типом)
        boolean wasReversal =
                (type.getDirection() == CashFlowDirection.EXPENSE && expense.signum() < 0)
                        || (type.getDirection() == CashFlowDirection.INCOME  && income.signum()  < 0);

        log.info("Operation:{} with income:{} or expence:{}", type, income, expense);
        if (income.signum() < 0) { expense = expense.add(income.negate()); income = BigDecimal.ZERO; }
        if (expense.signum() < 0) { income = income.add(expense.negate()); expense = BigDecimal.ZERO; }

        String platform = columnHeaders.platform(row, fmt);

        DividendNote dividendNote = type.hasDividendFeature()
                ? DividendNote.parse(columnHeaders.note(row, fmt))
                : null;

        List<CashFlowRow> result = new ArrayList<>(2);

        for (var candidate : List.of(
                Map.entry(CashFlowDirection.INCOME, income),
                Map.entry(CashFlowDirection.EXPENSE, expense))) {
            CashFlowDirection direction = candidate.getKey();
            BigDecimal amount = candidate.getValue();

            if (!isPositive(amount)) continue;

            // если направление НЕ совпадает с типом — пропускаем, КРОМЕ случая сторно
            boolean matchesType = type.getDirection() == null || type.getDirection() == direction;
            if (!matchesType && !wasReversal) continue;

            log.debug("parsed new CashFlowRow: date={}, type={}, direction={}, currency={}, amount={}",
                    date, type, direction, currency, amount);
            result.add(new CashFlowRow(date, type, direction, currency, platform, dividendNote, amount));
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

    private static boolean isPositive(BigDecimal v) {
        return Objects.nonNull(v) && v.compareTo(BigDecimal.ZERO) > 0;
    }

}