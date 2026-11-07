package ru.redcarpet.stocks.reports.cashflow;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.reports.common.CellReader;
import ru.redcarpet.stocks.reports.common.CurrencyParser;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Раздел 1.1 — операции с наличными (списания/зачисления). */
@Component
@RequiredArgsConstructor
public final class CashParser {

    private static final int CURRENCY_EXPECTED_COLUMN = 1;  // столбец B
    private static final int MAX_BLOCK_CELLS = 3;

    public CashParseResult parse(List<Row> rows, DataFormatter fmt, CashRowMapper mapper) {
        List<CashFlowRow> incomes = new ArrayList<>();
        List<CashFlowRow> expenses = new ArrayList<>();
        List<CashFlowRow> overnightInterests = new ArrayList<>();
        ColumnHeaders columns = new ColumnHeaders();
        Currency currentCurrency = null;

        for (Row row : rows) {
            // строка-маркер валютного блока: "EUR", "USD", "Рубль"
            Optional<Currency> block = detectCurrencyBlock(row, fmt);
            if (block.isPresent()) {
                currentCurrency = block.get();
                continue;
            }

            columns.tryUpdateCashFlow(row, fmt);
            if (!columns.isValid()) continue;

            List<CashFlowRow> ops = mapper.toCashOperations(row, fmt, columns, currentCurrency);
            for (CashFlowRow op : ops) {
                if (op.operation() == CashOperationType.INTEREST) {
                    overnightInterests.add(op);
                } else if (op.isIncome()) {
                    incomes.add(op);
                } else {
                    expenses.add(op);
                }
            }
        }
        return new CashParseResult(incomes, expenses, overnightInterests);
    }

    private static Optional<Currency> detectCurrencyBlock(Row row, DataFormatter fmt) {
        if (Objects.isNull(row)) return Optional.empty();
        Optional<Currency> ExpectedOpt = CurrencyParser
                .tryFromReport(CellReader.read(row, CURRENCY_EXPECTED_COLUMN, fmt));
        if (ExpectedOpt.isPresent()) return ExpectedOpt;

        List<String> cells = CellReader.readAll(row, fmt);
        long nonBlankCount = cells.stream().filter(v -> !v.isEmpty()).count();
        if (nonBlankCount > MAX_BLOCK_CELLS) return Optional.empty();
        return cells.stream()
                .map(CurrencyParser::tryFromReport)
                .flatMap(Optional::stream)
                .findFirst();
    }
}