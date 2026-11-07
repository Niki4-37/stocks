package ru.redcarpet.stocks.importing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.importing.dto.CashImportResult;
import ru.redcarpet.stocks.importing.dto.ImportSummarizedResult;
import ru.redcarpet.stocks.importing.dto.ImportResult;
import ru.redcarpet.stocks.model.dto.BalanceMismatch;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.reports.dto.ParseResult;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportImportFacade {

    private final PortfolioRepository portfolioRepository;
    private final CashImportService cashImportService;
    private final TradeImportService tradeImportService;
    private final CurrencyTradeImportService currencyTradeImportService;
    private final UpdateHoldingsService updateHoldingsService;
    private final BalanceService balanceService;

    @Transactional
    public ImportSummarizedResult importReport(Long portfolioId, ParseResult parsed) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException("Portfolio not found: " + portfolioId));

        CashImportResult cash = cashImportService.importFlows(
                portfolio.getId(), parsed.cashFlows().summarizedCash(), TradeSource.BROKER_REPORT);

        ImportResult trades = tradeImportService.importTrades(
                portfolio, parsed.trades());

        ImportResult dividends = updateHoldingsService.applySavedDividends(portfolio, cash.savedDividends());

        ImportResult currencies = currencyTradeImportService.importCurrencies(
                portfolio, parsed.currencies());

        balanceService.recalculate(portfolio);
        List<BalanceMismatch> mismatches = balanceService.check(portfolio.getId());

        return new ImportSummarizedResult(cash.result(), trades, dividends, currencies, mismatches);
    }
}