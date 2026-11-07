package ru.redcarpet.stocks.ui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.redcarpet.stocks.application.PortfolioFacade;
import ru.redcarpet.stocks.model.dto.CreatePortfolioCommand;
import ru.redcarpet.stocks.model.dto.PortfolioBasicInfo;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Controller
@RequestMapping("/ui/portfolios")
@RequiredArgsConstructor
public class PortfolioViewController {

    private final PortfolioFacade facade;

    @GetMapping
    public String list(Model model) {
        List<PortfolioBasicInfo> portfolios = facade.list();
        model.addAttribute("portfolios", portfolios);
        return "portfolios/list";
    }

    /** Форма создания */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute(
                "request",
                new CreatePortfolioCommand(
                    "Название портфеля",
                    Currency.getInstance("RUB"),
                    BigDecimal.ZERO
        ));
        return "portfolios/form";
    }

    /** Обработка формы */
    @PostMapping
    public String create(
        @ModelAttribute("request") CreatePortfolioCommand cmd,
        Model model
    ) {
        try {
            facade.create(cmd);
            return "redirect:/ui/portfolios";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "portfolios/form";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("portfolio", facade.details(id));
        return "portfolios/details";
    }
}
