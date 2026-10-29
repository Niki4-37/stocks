package ru.redcarpet.stocks.ui.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.redcarpet.stocks.importing.dto.ImportReportResult;
import ru.redcarpet.stocks.importing.service.ReportImportFacade;
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

    private static final String SESSION_PARSE_RESULT = "lastParseResult";
    private static final String SESSION_PORTFOLIO_ID = "lastPortfolioId";

    private final ReportParserService parser;
    private final TradeCheckRequestMapper checkMapper;
    private final TradeVerificationService verificationService;
    private final CashVerificationService cashVerificationService;
    private final CashCheckRequestMapper cashCheckMapper;
    private final ReportImportFacade facade;

    @GetMapping
    public String page(@RequestParam(required = false) Long portfolioId, Model model) {
        model.addAttribute("portfolioId", portfolioId);
        return "reports/report";
    }

    @PostMapping("/upload")
    public String upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "portfolioId", required = false) Long portfolioId,
            HttpSession session,
            Model model
    ) {
        try {
            ParseResult res = parser.parse(file);

            session.setAttribute(SESSION_PARSE_RESULT, res);
            session.setAttribute(SESSION_PORTFOLIO_ID, portfolioId);

            model.addAttribute("portfolioId", portfolioId);
            model.addAttribute("fileName", file.getOriginalFilename());
            model.addAttribute("cashIncomes", res.cashFlows().incomes());
            model.addAttribute("cashExpenses", res.cashFlows().expenses());
            model.addAttribute("overnightInterests", res.cashFlows().overnightInterests());
            model.addAttribute("overnightInterestsTotal", res.cashFlows().overnightInterestsTotal());
            model.addAttribute("checks", res.checks());
            model.addAttribute("stocks",     res.trades().stocks());
            model.addAttribute("funds",      res.trades().funds());
            model.addAttribute("bonds",      res.trades().bonds());
            model.addAttribute("currencies", res.trades().currencies());

            if (Objects.nonNull(portfolioId)) {
                TradesResult trades = new TradesResult(
                        res.trades().stocks(),
                        res.trades().funds(),
                        res.trades().bonds(),
                        res.trades().currencies());
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
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        ParseResult parsed = parser.parse(file);
        TradesResult trades = new TradesResult(
                parsed.trades().stocks(),
                parsed.trades().funds(),
                parsed.trades().bonds(),
                parsed.trades().currencies());

        CheckTradesRequest request = checkMapper.toCheckRequest(portfolioId, trades);
        return verificationService.check(request);
    }

    @PostMapping("/upload/import")
    public String importAll(@RequestParam Long portfolioId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes
    ) {
        ParseResult parsed = (ParseResult) session.getAttribute(SESSION_PARSE_RESULT);
        if (parsed == null) {
            redirectAttributes.addFlashAttribute("error", "Нет данных для импорта — сначала загрузите отчёт");
            return "redirect:/ui/reports?portfolioId=" + portfolioId;
        }

        try {
            ImportReportResult result = facade.importReport(portfolioId, parsed);

            session.removeAttribute(SESSION_PARSE_RESULT);
            session.removeAttribute(SESSION_PORTFOLIO_ID);

            redirectAttributes.addFlashAttribute("importResult", result);
            return "redirect:/ui/portfolios/" + portfolioId;

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Импорт не выполнен: " + e.getMessage());
            return "redirect:/ui/reports?portfolioId=" + portfolioId;
        }
    }
}
