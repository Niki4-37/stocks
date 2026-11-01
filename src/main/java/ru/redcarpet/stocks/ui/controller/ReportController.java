package ru.redcarpet.stocks.ui.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.redcarpet.stocks.importing.dto.ImportReportResult;
import ru.redcarpet.stocks.importing.service.ReportImportFacade;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.reports.dto.ParseResult;
import ru.redcarpet.stocks.reports.service.ReportParserService;
import ru.redcarpet.stocks.ui.component.ReportViewModelBuilder;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/ui/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final String SESSION_PARSE_RESULT = "lastParseResult";

    private final ReportParserService reportParserService;
    private final ReportViewModelBuilder reportViewModelBuilder;
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
            ParseResult parsed = reportParserService.parse(file);

            session.setAttribute(SESSION_PARSE_RESULT, parsed);

            model.addAttribute("fileName", file.getOriginalFilename());
            reportViewModelBuilder.fill(model, parsed, portfolioId);
        } catch (IOException | ValidationException e) {
            log.warn("Failed to parse report", e);
            model.addAttribute("error", "Не удалось прочитать файл: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected error parsing report", e);
            model.addAttribute("error", "Внутренняя ошибка при чтении отчёта");
        }
        return "reports/report";
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

            redirectAttributes.addFlashAttribute("importResult", result);
            return "redirect:/ui/portfolios/" + portfolioId;

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Импорт не выполнен: " + e.getMessage());
            return "redirect:/ui/reports?portfolioId=" + portfolioId;
        }
    }
}
