package ru.redcarpet.stocks.reports.cashflow;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.model.converter.CurrencyConverter;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.reports.common.CellReader;
import ru.redcarpet.stocks.reports.common.CurrencyParser;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/** Раздел 1.1 — операции с наличными (списания/зачисления). */
@Component
@RequiredArgsConstructor
public final class CashParser {

    public CashParseResult parse(List<Row> rows, DataFormatter fmt, CashRowMapper mapper) {
        List<CashFlowRow> incomes = new ArrayList<>();
        List<CashFlowRow> expenses = new ArrayList<>();
        List<CashFlowRow> overnightInterests = new ArrayList<>();
        ColumnHeaders columns = new ColumnHeaders();
        Currency currentCurrency = null;

        for (Row row : rows) {
            // строка-маркер валютного блока: "EUR", "USD", "Рубль"
            Currency blockCurrency = detectCurrencyBlock(row, fmt);
            if (Objects.nonNull(blockCurrency)) {
                currentCurrency = blockCurrency;
                continue;
            }

            columns.tryUpdateCashFlow(row, fmt);
            if (!columns.isValid()) continue;

            CashFlowRow op = mapper.toRubCashOperation(row, fmt, columns, currentCurrency);
            if (Objects.isNull(op)) continue;

            if (CashOperationType.isOvernightInterest(op.operation())) {
                overnightInterests.add(op);
            } else if (op.direction() == CashFlowDirection.INCOME) {
                incomes.add(op);
            } else {
                expenses.add(op);
            }
        }
        return new CashParseResult(incomes, expenses, overnightInterests);
    }

    private static Currency detectCurrencyBlock(Row row, DataFormatter fmt) {
        Currency found = null;
        int nonBlank = 0;

        for (Cell c : row) {
            String v = fmt.formatCellValue(c).trim();
            if (v.isEmpty()) continue;

            nonBlank++;

            if (found == null) {
                try {
                    found = CurrencyParser.fromReport(v);
                } catch (ValidationException ignored) {
                    // не валюта — это нормально для обычной строки
                }
            }
        }

        // блок валюты — строка с малым числом непустых ячеек и распознанной валютой
        return (nonBlank <= 2) ? found : null;
    }
}