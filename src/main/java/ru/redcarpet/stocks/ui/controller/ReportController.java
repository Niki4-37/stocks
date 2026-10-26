package ru.redcarpet.stocks.ui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.redcarpet.stocks.importing.CashImportService;
import ru.redcarpet.stocks.reports.dto.ParseResult;
import ru.redcarpet.stocks.reports.dto.TradesResult;
import ru.redcarpet.stocks.reports.service.ReportParserService;
import ru.redcarpet.stocks.verification.trades.TradeSource;
import ru.redcarpet.stocks.verification.trades.TradeVerificationService;
import ru.redcarpet.stocks.verification.trades.TradeCheckRequestMapper;
import ru.redcarpet.stocks.verification.trades.CheckTradesRequest;
import ru.redcarpet.stocks.verification.trades.CheckTradesResponse;
import ru.redcarpet.stocks.verification.cash.CashCheckRequestMapper;
import ru.redcarpet.stocks.verification.cash.CashVerificationService;
import ru.redcarpet.stocks.verification.cash.CashCheckRequest;

import java.util.Objects;

@Controller
@RequestMapping("/ui/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportParserService parser;
    private final TradeCheckRequestMapper checkMapper;
    private final TradeVerificationService verificationService;
    private final CashVerificationService cashVerificationService;
    private final CashCheckRequestMapper cashCheckMapper;
    private final CashImportService importService;

    @GetMapping
    public String page(@RequestParam(required = false) Long portfolioId, Model model) {
        model.addAttribute("portfolioId", portfolioId);
        return "reports/report";
    }

    @PostMapping("/upload")
    public String upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "portfolioId", required = false) Long portfolioId,
            Model model
    ) {
        try {
            ParseResult res = parser.parse(file);
            model.addAttribute("fileName", file.getOriginalFilename());
            model.addAttribute("cashIncomes", res.cashFlows().incomes());
            model.addAttribute("cashExpenses", res.cashFlows().expenses());
            model.addAttribute("overnightInterests", res.cashFlows().overnightInterests());
            model.addAttribute("overnightInterestsTotal", res.cashFlows().overnightInterestsTotal());
            model.addAttribute("checks", res.checks());
            model.addAttribute("stocks",     res.stocks());
            model.addAttribute("funds",      res.funds());
            model.addAttribute("bonds",      res.bonds());
            model.addAttribute("currencies", res.currencies());
            model.addAttribute("portfolioId", portfolioId);

            if (Objects.nonNull(portfolioId)) {
                TradesResult trades = new TradesResult(
                        res.stocks(), res.funds(), res.bonds(), res.currencies());
                CheckTradesRequest request = checkMapper.toCheckRequest(portfolioId, trades);
                CheckTradesResponse verification = verificationService.check(request);
                model.addAttribute("verification", verification);

                CashCheckRequest flowsReq = cashCheckMapper.toCheckRequest(
                        portfolioId, res.cashFlows(), TradeSource.BROKER_REPORT);
                model.addAttribute("cashVerification", cashVerificationService.check(flowsReq));
            }

        } catch (Exception e) {
            model.addAttribute("error", "Не удалось прочитать файл: " + e.getMessage());
        }
        return "reports/report";
    }

    @PostMapping("/{portfolioId}/verify-trades")
    public CheckTradesResponse verifyTrades(
            @PathVariable Long portfolioId,
            @RequestParam("file") MultipartFile file) throws Exception {

        ParseResult parsed = parser.parse(file);
        TradesResult trades = new TradesResult(
                parsed.stocks(), parsed.funds(), parsed.bonds(), parsed.currencies());

        CheckTradesRequest request = checkMapper.toCheckRequest(portfolioId, trades);
        return verificationService.check(request);
    }

    @PostMapping("/upload/import-cash")
    public String importCash(@RequestParam Long portfolioId,
                             @RequestParam("file") MultipartFile file,
                             Model model) throws Exception {
        ParseResult parsed = parser.parse(file);
        int inserted = importService.importFlows(
                portfolioId, parsed.cashFlows().summarizedCash(), TradeSource.BROKER_REPORT);
        model.addAttribute("importedCount", inserted);
        return "redirect:/ui/portfolios/" + portfolioId;
    }
}
