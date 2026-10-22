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
import ru.redcarpet.stocks.reports.utils.ColumnLayout;
import ru.redcarpet.stocks.reports.utils.RowClassifier;
import ru.redcarpet.stocks.reports.utils.RowMapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportParserService {

    public ParseResult parse(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             Workbook wb = WorkbookFactory.create(is)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            List<OperationRow> details = collectDetails(sheet, fmt);
            Map<String, BigDecimal> totals = collectTotals13(sheet, fmt);
            List<CheckRow> checks = buildChecks(details, totals);

            return new ParseResult(details, checks);
        }
    }

    private List<OperationRow> collectDetails(Sheet sheet, DataFormatter fmt) {
        List<OperationRow> result = new ArrayList<>();
        ColumnLayout layout = new ColumnLayout();
        RowMapper mapper = new RowMapper(layout);
        RowClassifier classifier = new RowClassifier();

        for (Row row : sheet) {
            TableSection section = classifier.classify(row, fmt);
            layout.tryUpdate(row, fmt);
            if (section != TableSection.DETAILS) continue;

            OperationRow d = mapper.toDetail(row, fmt);
            if (d != null) result.add(d);
        }
        return result;
    }

    private Map<String, BigDecimal> collectTotals13(Sheet sheet, DataFormatter fmt) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        ColumnLayout layout = new ColumnLayout();
        RowMapper mapper = new RowMapper(layout);
        RowClassifier classifier = new RowClassifier();

        for (Row row : sheet) {
            TableSection section = classifier.classify(row, fmt);
            layout.tryUpdate(row, fmt);
            if (section != TableSection.TOTALS_13) continue;

            TotalsEntry entry = mapper.toTotalsEntry(row, fmt);
            if (entry == null) continue;

            BigDecimal val = toDecimal(entry.amount());
            if (val == null) continue;

            totals.merge(key(entry.operation(), entry.platform()),
                    val, BigDecimal::add);
        }
        return totals;
    }

    private List<CheckRow> buildChecks(List<OperationRow> details,
                                       Map<String, BigDecimal> totals13) {
        Map<String, BigDecimal> detailSums = new LinkedHashMap<>();
        for (OperationRow r : details) {
            BigDecimal v = toDecimal(r.amount());
            if (v == null) continue;
            detailSums.merge(key(r.operation(), r.platform()),
                    v, BigDecimal::add);
        }

        List<CheckRow> checks = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> e : totals13.entrySet()) {
            String[] parts = splitKey(e.getKey());
            BigDecimal detail = detailSums.getOrDefault(e.getKey(), BigDecimal.ZERO);
            checks.add(new CheckRow(parts[0], parts[1], detail, e.getValue(),
                    detail.compareTo(e.getValue()) == 0));
        }
        for (Map.Entry<String, BigDecimal> e : detailSums.entrySet()) {
            if (totals13.containsKey(e.getKey())) continue;
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
