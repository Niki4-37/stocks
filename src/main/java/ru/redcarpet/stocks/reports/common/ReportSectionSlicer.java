package ru.redcarpet.stocks.reports.common;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.reports.dto.SectionRows;

import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.List;

/** Один проход по листу: раскладывает строки по разделам отчёта. */
@Component
public final class ReportSectionSlicer {

    public SectionRows slice(Sheet sheet, DataFormatter fmt) {
        List<Row> cash = new ArrayList<>();
        List<Row> fees = new ArrayList<>();
        List<Row> trades = new ArrayList<>();

        ReportSectionClassifier classifier = new ReportSectionClassifier();

        for (Row row : sheet) {
            switch (classifier.classify(row, fmt)) {
                case CASH_OPERATION           -> cash.add(row);
                case TOTAL_FEES_AND_PENALTIES -> fees.add(row);
                case TRADES                   -> trades.add(row);
                default -> { }
            }
        }
        return new SectionRows(cash, fees, trades);
    }
}