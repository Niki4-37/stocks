package ru.redcarpet.stocks.ui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.redcarpet.stocks.model.service.PortfolioService;
import ru.redcarpet.stocks.ui.dto.PortfolioForm;
import ru.redcarpet.stocks.model.dto.PortfolioResponse;

import java.util.List;

@Controller
@RequestMapping("/ui/portfolios")
@RequiredArgsConstructor
public class PortfolioViewController {

    private final PortfolioService portfolioService;

    @GetMapping
    public String list(Model model) {
        List<PortfolioResponse> portfolios = portfolioService.findAll();
        model.addAttribute("portfolios", portfolios);
        return "portfolios/list";
    }

    /** Форма создания */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("request", new PortfolioForm());
        return "portfolios/form";
    }

    /** Обработка формы */
    @PostMapping
    public String create(
        @ModelAttribute("request") PortfolioForm form,
        Model model
    ) {
        try {
            portfolioService.create(form.toRequest());
            return "redirect:/ui/portfolios";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "portfolios/form";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("portfolio", portfolioService.findById(id));
        return "portfolios/details";
    }
}
