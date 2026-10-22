package ru.redcarpet.stocks.reports.service;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.redcarpet.stocks.reports.dto.CheckRow;
import ru.redcarpet.stocks.reports.dto.OperationRow;
import ru.redcarpet.stocks.reports.dto.ParseResult;
import ru.redcarpet.stocks.reports.dto.TotalsEntry;
import ru.redcarpet.stocks.reports.enums.TableSection;
import ru.redcarpet.stocks.reports.utils.ColumnHeaders;
import ru.redcarpet.stocks.reports.utils.RowSectionClassifier;
import ru.redcarpet.stocks.reports.utils.RowMapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ReportParserService {

    public ParseResult parse(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             Workbook wb = WorkbookFactory.create(is)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            List<OperationRow> cashOperations = collectRubCashOperations(sheet, fmt);
            Map<String, BigDecimal> totals = collectFeesAndPenalties(sheet, fmt);
            List<CheckRow> checks = buildFeesChecks(cashOperations, totals);

            return new ParseResult(cashOperations, checks);
        }
    }

    private List<OperationRow> collectRubCashOperations(Sheet sheet, DataFormatter fmt) {
        List<OperationRow> result = new ArrayList<>();
        ColumnHeaders columnHeaders = new ColumnHeaders();
        RowMapper mapper = new RowMapper(columnHeaders);
        RowSectionClassifier classifier = new RowSectionClassifier();

        for (Row row : sheet) {
            TableSection section = classifier.classify(row, fmt);
            columnHeaders.update(row, fmt);
            if (section != TableSection.CASH_OPERATION) continue;

            OperationRow operation = mapper.toDetail(row, fmt);
            if (Objects.nonNull(operation)) result.add(operation);
        }
        return result;
    }

    private Map<String, BigDecimal> collectFeesAndPenalties(Sheet sheet, DataFormatter fmt) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        ColumnHeaders columnHeaders = new ColumnHeaders();
        RowMapper mapper = new RowMapper(columnHeaders);
        RowSectionClassifier classifier = new RowSectionClassifier();

        for (Row row : sheet) {
            TableSection section = classifier.classify(row, fmt);
            columnHeaders.update(row, fmt);
            if (section != TableSection.TOTAL_FEES_AND_PENALTIES) continue;

            TotalsEntry entry = mapper.toTotalsEntry(row, fmt);
            if (Objects.isNull(entry)) continue;

            BigDecimal val = toDecimal(entry.amount());
            if (Objects.isNull(val)) continue;

            totals.merge(key(entry.operation(), entry.platform()),
                    val, BigDecimal::add);
        }
        return totals;
    }

    private List<CheckRow> buildFeesChecks(List<OperationRow> details,
                                           Map<String, BigDecimal> totals) {
        Map<String, BigDecimal> detailSums = new LinkedHashMap<>();
        for (OperationRow row : details) {
            BigDecimal value = toDecimal(row.amount());
            if (Objects.isNull(value)) continue;
            detailSums.merge(key(row.operation(), row.platform()),
                    value, BigDecimal::add);
        }

        List<CheckRow> checks = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> e : totals.entrySet()) {
            String[] parts = splitKey(e.getKey());
            BigDecimal detail = detailSums.getOrDefault(e.getKey(), BigDecimal.ZERO);
            checks.add(new CheckRow(parts[0], parts[1], detail, e.getValue(),
                    detail.compareTo(e.getValue()) == 0));
        }
        for (Map.Entry<String, BigDecimal> e : detailSums.entrySet()) {
            if (totals.containsKey(e.getKey())) continue;
            String[] parts = splitKey(e.getKey());
            checks.add(new CheckRow(parts[0], parts[1],
                    e.getValue(), BigDecimal.ZERO, false));
        }
        return checks;
    }

    // ------------------------------------------------------------------
    //  Small helpers
    // ------------------------------------------------------------------

    private static String key(String op, String platform) {
        return op + "|" + platform;
    }

    private static String[] splitKey(String key) {
        return key.split("\\|", 2);
    }

    private BigDecimal toDecimal(String s) {
        if (s == null) return null;
        String cleaned = s.replace('\u00A0', ' ')
                .replace(" ", "")
                .replace(",", ".");
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
