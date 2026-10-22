package ru.redcarpet.stocks.ui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.redcarpet.stocks.reports.dto.ParseResult;
import ru.redcarpet.stocks.reports.service.ReportParserService;

@Controller
@RequestMapping("/ui/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportParserService parser;

    @GetMapping
    public String page() {
        return "reports/reports";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            ParseResult res = parser.parse(file);
            model.addAttribute("rows", res.rows());
            model.addAttribute("checks", res.checks());
            model.addAttribute("fileName", file.getOriginalFilename());
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось прочитать файл: " + e.getMessage());
        }
        return "reports/reports";
    }
}
