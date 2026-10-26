package ru.redcarpet.stocks.reports.cashflow;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;
import ru.redcarpet.stocks.reports.dto.TotalsEntry;
import ru.redcarpet.stocks.reports.enums.RowOperationNames;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public final class CahsOperationRowMapper {

    private static final Pattern DATE_PATTERN =
            Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{2}");

    private final ColumnHeaders columnHeaders;

    public CahsOperationRowMapper(ColumnHeaders columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    /** Детальная строка раздела 1.1 или null, если не подходит. */
    public CashFlowRow toRubCashOperation(Row row, DataFormatter fmt) {
        if (!columnHeaders.isValid() || !columnHeaders.hasDate()) return null;

        String date = columnHeaders.date(row, fmt);
        if (!DATE_PATTERN.matcher(date).matches()) return null;

        String op = columnHeaders.operation(row, fmt);
        if (!RowOperationNames.contains(op)) return null;

        String incomeStr  = columnHeaders.income(row, fmt);
        String expenseStr = columnHeaders.expense(row, fmt);

        BigDecimal income  = DecimalParser.parse(incomeStr);
        BigDecimal expense = DecimalParser.parse(expenseStr);

        boolean hasIncome  = Objects.nonNull(income) && income.compareTo(BigDecimal.ZERO) > 0;
        boolean hasExpense = Objects.nonNull(expense) && expense.compareTo(BigDecimal.ZERO) > 0;
        if (!hasIncome && !hasExpense) return null;

        if (RowOperationNames.isIncome(op) && !hasIncome) return null;
        if (RowOperationNames.isExpense(op) && !hasExpense) return null;

        String platform = columnHeaders.platform(row, fmt);
        if (platform.isBlank()) return null;

        DividendNote dividendNote = null;
        String noteRaw = Strings.EMPTY;
        if (RowOperationNames.hasNote(op)) {
            String note = columnHeaders.note(row, fmt);
            if (!note.isBlank()) {
                dividendNote = DividendNote.parse(note);
                noteRaw = note;
            }
        }

        CashFlowRow.Direction direction =
                hasExpense ? CashFlowRow.Direction.EXPENSE : CashFlowRow.Direction.INCOME;
        String amount = hasExpense ? expenseStr : incomeStr;

        return new CashFlowRow(
                date, op, direction, amount, platform, noteRaw, dividendNote,
                hasIncome ? incomeStr : "",
                hasExpense ? expenseStr : ""
        );
    }

    /** Итоговая строка 1.3 или null, если не подходит. */
    public TotalsEntry toTotalsEntry(Row row, DataFormatter fmt) {
        if (!columnHeaders.isValid()) return null;

        String op = columnHeaders.operation(row, fmt);
        Optional<RowOperationNames> opOpt = RowOperationNames.byTitle(op);
        if (opOpt.isEmpty()) return null;
        RowOperationNames operation = opOpt.get();
        if (operation.flow() != RowOperationNames.Flow.EXPENSE) return null;

        String amount = columnHeaders.amount(row, fmt);
        if (amount.isBlank()) return null;

        String platform = columnHeaders.platform(row, fmt);
        if (platform.isBlank()) return null;

        return new TotalsEntry(op, platform, amount);
    }
}