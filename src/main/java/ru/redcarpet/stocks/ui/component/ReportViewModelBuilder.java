package ru.redcarpet.stocks.ui.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import ru.redcarpet.stocks.reports.dto.ParseResult;
import ru.redcarpet.stocks.verification.cash.CashCheckRequest;
import ru.redcarpet.stocks.verification.cash.CashCheckRequestMapper;
import ru.redcarpet.stocks.verification.cash.CashVerificationService;
import ru.redcarpet.stocks.verification.trades.TradeCheckRequestMapper;
import ru.redcarpet.stocks.verification.trades.TradeSource;
import ru.redcarpet.stocks.verification.trades.TradeVerificationService;
import ru.redcarpet.stocks.verification.trades.TradesCheckRequest;

@Component
@RequiredArgsConstructor
public class ReportViewModelBuilder {

    private final TradeCheckRequestMapper tradeCheckRequestMapper;
    private final TradeVerificationService tradeVerificationService;
    private final CashCheckRequestMapper cashCheckRequestMapper;
    private final CashVerificationService cashVerificationService;

    /** Заполняет модель для страницы отчёта: данные + сверка (если portfolioId != null). */
    public void fill(Model model, ParseResult parsed, Long portfolioId) {
        model.addAttribute("portfolioId", portfolioId);

        // потоки 1.1
        model.addAttribute("cashIncomes", parsed.cashFlows().incomes());
        model.addAttribute("cashExpenses", parsed.cashFlows().expenses());
        model.addAttribute("overnightInterests", parsed.cashFlows().overnightInterests());
        model.addAttribute("overnightInterestsTotal", parsed.cashFlows().overnightInterestsTotal());
        model.addAttribute("checks", parsed.checks());

        // сделки 2.1
        model.addAttribute("stocks", parsed.trades().stocks());
        model.addAttribute("funds", parsed.trades().funds());
        model.addAttribute("bonds", parsed.trades().bonds());
        model.addAttribute("currencies", parsed.currencies());

        if (portfolioId == null) return;

        // сверка сделок
        TradesCheckRequest tradesReq = tradeCheckRequestMapper
                .toCheckRequest(portfolioId, parsed.trades(), parsed.currencies());
        model.addAttribute("verification", tradeVerificationService.check(tradesReq));

        // сверка потоков
        CashCheckRequest cashReq = cashCheckRequestMapper
                .toCheckRequest(portfolioId, parsed.cashFlows(), TradeSource.BROKER_REPORT);
        model.addAttribute("cashVerification", cashVerificationService.check(cashReq));
    }
}