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
import ru.redcarpet.stocks.reports.dto.SectionRows;
import ru.redcarpet.stocks.reports.dto.SecurityRef;
import ru.redcarpet.stocks.reports.dto.StockTrade;
import ru.redcarpet.stocks.reports.dto.TotalsEntry;
import ru.redcarpet.stocks.reports.dto.TradesResult;
import ru.redcarpet.stocks.reports.enums.ReportSection;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;
import ru.redcarpet.stocks.reports.utils.ColumnHeaders;
import ru.redcarpet.stocks.reports.utils.ReportSectionClassifier;
import ru.redcarpet.stocks.reports.utils.RowMapper;
import ru.redcarpet.stocks.reports.utils.TradeSubsectionClassifier;
import ru.redcarpet.stocks.reports.utils.trades.CurrencyRefParser;
import ru.redcarpet.stocks.reports.utils.trades.SecurityRefParser;
import ru.redcarpet.stocks.reports.utils.trades.TradeLayout;
import ru.redcarpet.stocks.reports.utils.trades.TradeLayoutFactory;

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

            SectionRows slices = sliceSections(sheet, fmt);

            List<OperationRow> cash = collectRubCashOperations(slices.cashOperations(), fmt);
            Map<String, BigDecimal> fees = collectFeesAndPenalties(slices.feesAndPenalties(), fmt);
            TradesResult trades = collectTrades(slices.trades(), fmt);
            List<CheckRow> checks = buildFeesChecks(cash, fees);

            return new ParseResult(cash, trades.stocks(), trades.funds(), trades.currencies(), checks);
        }
    }

    private SectionRows sliceSections(Sheet sheet, DataFormatter fmt) {
        List<Row> cash = new ArrayList<>();
        List<Row> fees = new ArrayList<>();
        List<Row> trades = new ArrayList<>();

        ReportSectionClassifier sectionClassifier = new ReportSectionClassifier();

        for (Row row : sheet) {
            ReportSection section = sectionClassifier.classify(row, fmt);
            switch (section) {
                case CASH_OPERATION         -> cash.add(row);
                case TOTAL_FEES_AND_PENALTIES -> fees.add(row);
                case TRADES                 -> trades.add(row);
                default -> { /* BLANK и прочее — пропускаем */ }
            }
        }
        return new SectionRows(cash, fees, trades);
    }

    private List<OperationRow> collectRubCashOperations(List<Row> rubCashOperationRows, DataFormatter fmt) {
        List<OperationRow> result = new ArrayList<>();
        ColumnHeaders columnHeaders = new ColumnHeaders();
        RowMapper mapper = new RowMapper(columnHeaders);

        for (Row row : rubCashOperationRows) {
            columnHeaders.update(row, fmt);
            if (!columnHeaders.isValid()) continue;
            OperationRow operation = mapper.toRubCashOperation(row, fmt);
            if (Objects.nonNull(operation)) result.add(operation);
        }
        return result;
    }

    private Map<String, BigDecimal> collectFeesAndPenalties(List<Row> feesAndPenaltiesRows, DataFormatter fmt) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        ColumnHeaders columnHeaders = new ColumnHeaders();
        RowMapper mapper = new RowMapper(columnHeaders);

        for (Row row : feesAndPenaltiesRows) {
            columnHeaders.update(row, fmt);
            if (!columnHeaders.isValid()) continue;
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

    private TradesResult collectTrades(List<Row> tradeRows, DataFormatter fmt) {
        TradeLayout<?> tradeLayout = null;
        SecurityRef currentSecurityRef = SecurityRef.EMPTY;
        TradeSubsectionClassifier subsectionClassifier = new TradeSubsectionClassifier();
        List<StockTrade> stocks = new ArrayList<>();
        List<FundTrade> funds = new ArrayList<>();
        List<CurrencyTrade> currencies = new ArrayList<>();
        for (Row row : tradeRows) {
            TradeSubsection subSection = subsectionClassifier.classify(row, fmt);

            // 1) подраздел сменился → переключаем стратегию
            if (Objects.isNull(tradeLayout) || !tradeLayout.isSupports(subSection)) {
                tradeLayout = TradeLayoutFactory.create(subSection);
                currentSecurityRef = SecurityRef.EMPTY;
                if (Objects.isNull(tradeLayout)) continue;
            }

            // 2) строка-идентификатор
            SecurityRef parsed = (subSection == TradeSubsection.CURRENCY)
                    ? CurrencyRefParser.tryParse(row, fmt)
                    : SecurityRefParser.tryParse(row, fmt);
            if (Objects.nonNull(parsed)) {
                currentSecurityRef = parsed;
                continue;
            }

            // 3) шапка
            if (tradeLayout.tryUpdate(row, fmt)) continue;

            // 4) строка сделки — раскидываем по типизированным спискам
            if (!tradeLayout.isValid()) continue;

            Object trade = tradeLayout.toTrade(row, fmt, currentSecurityRef);
            if (trade instanceof StockTrade s)          stocks.add(s);
            else if (trade instanceof FundTrade f)      funds.add(f);
            else if (trade instanceof CurrencyTrade c)  currencies.add(c);
        }
        return new TradesResult(stocks, funds, currencies);
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
