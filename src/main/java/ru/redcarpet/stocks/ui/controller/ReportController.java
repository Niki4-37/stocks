package ru.redcarpet.stocks.ui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;
import ru.redcarpet.stocks.reports.dto.ParseResult;
import ru.redcarpet.stocks.reports.service.ReportParserService;

import java.util.List;

@Controller
@RequestMapping("/ui/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportParserService parser;

    @GetMapping
    public String page() {
        return "reports/report";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            ParseResult res = parser.parse(file);
            List<CashFlowRow> cash = res.cashFlows();
            List<CashFlowRow> incomes = cash.stream()
                    .filter(r -> r.direction() == CashFlowRow.Direction.INCOME).toList();
            List<CashFlowRow> expenses = cash.stream()
                    .filter(r -> r.direction() == CashFlowRow.Direction.EXPENSE).toList();
            model.addAttribute("cashIncomes", incomes);
            model.addAttribute("cashExpenses", expenses);
            model.addAttribute("checks", res.checks());
            model.addAttribute("stocks",     res.stocks());
            model.addAttribute("funds",      res.funds());
            model.addAttribute("bonds",      res.bonds());
            model.addAttribute("currencies", res.currencies());
            model.addAttribute("fileName", file.getOriginalFilename());
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось прочитать файл: " + e.getMessage());
        }
        return "reports/report";
    }
}
