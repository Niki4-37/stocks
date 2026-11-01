package ru.redcarpet.stocks.reports.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.redcarpet.stocks.reports.cashflow.CashParseResult;
import ru.redcarpet.stocks.reports.cashflow.CashParser;
import ru.redcarpet.stocks.reports.cashflow.CashRowMapper;
import ru.redcarpet.stocks.reports.cashflow.FeesChecksBuilder;
import ru.redcarpet.stocks.reports.common.ReportSectionSlicer;
import ru.redcarpet.stocks.reports.dto.CheckRow;
import ru.redcarpet.stocks.reports.dto.ParseResult;
import ru.redcarpet.stocks.reports.dto.SectionRows;
import ru.redcarpet.stocks.reports.dto.TradeParseResult;
import ru.redcarpet.stocks.reports.cashflow.FeesAndPenaltiesParser;
import ru.redcarpet.stocks.reports.trades.TradesParser;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportParserService {

    private final ReportSectionSlicer slicer;
    private final CashParser cashParser;
    private final FeesAndPenaltiesParser feesParser;
    private final TradesParser tradesParser;
    private final FeesChecksBuilder checksBuilder;
    private final CashRowMapper mapper;

    public ParseResult parse(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             Workbook wb = WorkbookFactory.create(is)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            SectionRows sections = slicer.slice(sheet, fmt);

            CashParseResult cashFlows = cashParser.parse(sections.cashOperations(), fmt, mapper);
            Map<String, BigDecimal> totals = feesParser.parse(sections.feesAndPenalties(), fmt, mapper);
            TradeParseResult trades = tradesParser.parse(sections.trades(), fmt);
            List<CheckRow> checks = checksBuilder.build(cashFlows.expenses(), totals);

            return new ParseResult(cashFlows, trades.papers(), trades.currencies(), checks);
        }
    }
}
