package ru.redcarpet.stocks.importing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.importing.dto.ImportReportResult;
import ru.redcarpet.stocks.importing.dto.ImportResult;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;
import ru.redcarpet.stocks.reports.dto.ParseResult;
import ru.redcarpet.stocks.verification.trades.TradeSource;

@Service
@RequiredArgsConstructor
public class ReportImportFacade {

    private final PortfolioRepository portfolioRepository;
    private final CashImportService cashImportService;
    private final TradeImportService tradeImportService;

    @Transactional
    public ImportReportResult importReport(Long portfolioId, ParseResult parsed) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException("Portfolio not found: " + portfolioId));

        ImportResult cashInserted = cashImportService.importFlows(
                portfolio.getId(), parsed.cashFlows().summarizedCash(), TradeSource.BROKER_REPORT);

        ImportResult tradesInserted = tradeImportService.importTrades(
                portfolio, parsed.trades());

        return new ImportReportResult(cashInserted, tradesInserted);
    }
}