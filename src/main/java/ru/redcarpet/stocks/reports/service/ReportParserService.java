package ru.redcarpet.stocks.reports.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.redcarpet.stocks.reports.cash.CashOperationParser;
import ru.redcarpet.stocks.reports.checks.FeesChecksBuilder;
import ru.redcarpet.stocks.reports.common.ReportSlicer;
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
import ru.redcarpet.stocks.reports.common.ColumnHeaders;
import ru.redcarpet.stocks.reports.common.ReportSectionClassifier;
import ru.redcarpet.stocks.reports.common.RowMapper;
import ru.redcarpet.stocks.reports.fees.FeesAndPenaltiesParser;
import ru.redcarpet.stocks.reports.trades.TradeSubsectionClassifier;
import ru.redcarpet.stocks.reports.trades.CurrencyRefParser;
import ru.redcarpet.stocks.reports.trades.SecurityRefParser;
import ru.redcarpet.stocks.reports.trades.TradeLayout;
import ru.redcarpet.stocks.reports.trades.TradeLayoutFactory;
import ru.redcarpet.stocks.reports.trades.TradesParser;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportParserService {

    private final ReportSlicer slicer;
    private final CashOperationParser cashOperationParser;
    private final FeesAndPenaltiesParser feesParser;
    private final TradesParser tradesParser;
    private final FeesChecksBuilder checksBuilder;

    public ParseResult parse(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             Workbook wb = WorkbookFactory.create(is)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            SectionRows sections = slicer.slice(sheet, fmt);

            List<OperationRow> operations = cashOperationParser.parse(sections.cashOperations(), fmt);
            Map<String, BigDecimal> totals = feesParser.parse(sections.feesAndPenalties(), fmt);
            TradesResult trades = tradesParser.parse(sections.trades(), fmt);
            List<CheckRow> checks = checksBuilder.build(operations, totals);

            return new ParseResult(operations, trades.stocks(), trades.funds(),
                    trades.currencies(), checks);
        }
    }
}
