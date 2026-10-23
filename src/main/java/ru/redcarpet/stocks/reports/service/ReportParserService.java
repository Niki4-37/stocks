package ru.redcarpet.stocks.reports.service;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.redcarpet.stocks.reports.dto.CheckRow;
import ru.redcarpet.stocks.reports.dto.CurrencyTrade;
import ru.redcarpet.stocks.reports.dto.FundTrade;
import ru.redcarpet.stocks.reports.dto.OperationRow;
import ru.redcarpet.stocks.reports.dto.ParseResult;
import ru.redcarpet.stocks.reports.dto.SecurityRef;
import ru.redcarpet.stocks.reports.dto.StockTrade;
import ru.redcarpet.stocks.reports.dto.TotalsEntry;
import ru.redcarpet.stocks.reports.dto.TradeRow;
import ru.redcarpet.stocks.reports.enums.TableSection;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;
import ru.redcarpet.stocks.reports.utils.ColumnHeaders;
import ru.redcarpet.stocks.reports.utils.ReportSectionClassifier;
import ru.redcarpet.stocks.reports.utils.RowMapper;
import ru.redcarpet.stocks.reports.utils.TradeSubsectionClassifier;
import ru.redcarpet.stocks.reports.utils.trades.CurrencyRefParser;
import ru.redcarpet.stocks.reports.utils.trades.CurrencyTradeLayout;
import ru.redcarpet.stocks.reports.utils.trades.FundTradeLayout;
import ru.redcarpet.stocks.reports.utils.trades.SecurityRefParser;
import ru.redcarpet.stocks.reports.utils.trades.StockTradeLayout;
import ru.redcarpet.stocks.reports.utils.trades.TradeLayout;

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

            List<StockTrade> stocks        = new ArrayList<>();
            List<FundTrade> funds          = new ArrayList<>();
            List<CurrencyTrade> currencies = new ArrayList<>();
            collectTrades(sheet, fmt, stocks, funds, currencies);

            return new ParseResult(cashOperations, stocks, funds, currencies, checks);
        }
    }

    private List<OperationRow> collectRubCashOperations(Sheet sheet, DataFormatter fmt) {
        List<OperationRow> result = new ArrayList<>();
        ColumnHeaders columnHeaders = new ColumnHeaders();
        RowMapper mapper = new RowMapper(columnHeaders);
        ReportSectionClassifier classifier = new ReportSectionClassifier();

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
        ReportSectionClassifier classifier = new ReportSectionClassifier();

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

    private void collectTrades(Sheet sheet,
                               DataFormatter fmt,
                               List<StockTrade> stocks,
                               List<FundTrade> funds,
                               List<CurrencyTrade> currencies) {

        TradeLayout<?> layout = null;
        SecurityRef current = SecurityRef.EMPTY;
        ReportSectionClassifier sectionClassifier = new ReportSectionClassifier();
        TradeSubsectionClassifier subsectionClassifier = new TradeSubsectionClassifier();

        for (Row row : sheet) {
            TableSection section = sectionClassifier.classify(row, fmt);
            if (section != TableSection.TRADES) {
                layout = null;
                current = SecurityRef.EMPTY;
                continue;
            }

            TradeSubsection sub = subsectionClassifier.classify(row, fmt);

            // 1) подраздел сменился → переключаем стратегию
            if (layout == null || !layout.supports(sub)) {
                layout = switch (sub) {
                    case STOCK    -> new StockTradeLayout();
                    case FUND     -> new FundTradeLayout();
                    case CURRENCY -> new CurrencyTradeLayout();
                    case BOND, OVERNIGHT_CB, NONE -> null;
                };
                current = SecurityRef.EMPTY;
                if (layout == null) continue;
            }

            // 2) строка-идентификатор
            SecurityRef parsed = (sub == TradeSubsection.CURRENCY)
                    ? CurrencyRefParser.tryParse(row, fmt)
                    : SecurityRefParser.tryParse(row, fmt);
            if (parsed != null) {
                current = parsed;
                continue;
            }

            // 3) шапка
            if (layout.tryUpdate(row, fmt)) continue;

            // 4) строка сделки — раскидываем по типизированным спискам
            if (!layout.isValid()) continue;

            Object trade = layout.toTrade(row, fmt, current);
            if (trade instanceof StockTrade s)          stocks.add(s);
            else if (trade instanceof FundTrade f)      funds.add(f);
            else if (trade instanceof CurrencyTrade c)  currencies.add(c);
        }
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
