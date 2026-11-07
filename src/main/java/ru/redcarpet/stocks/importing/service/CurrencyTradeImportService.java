package ru.redcarpet.stocks.importing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.importing.dto.ImportResult;
import ru.redcarpet.stocks.model.dto.CreateCurrencyExchangeRequest;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.enums.CurrencyTradeOperation;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.repositories.CurrencyTradeRepository;
import ru.redcarpet.stocks.model.service.CurrencyService;
import ru.redcarpet.stocks.reports.common.CurrencyParser;
import ru.redcarpet.stocks.reports.common.DateParser;
import ru.redcarpet.stocks.reports.common.DecimalParser;
import ru.redcarpet.stocks.reports.dto.CurrencyTrade;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyTradeImportService {

    private final CurrencyService currencyTradeService;
    private final CurrencyTradeRepository repository;

    @Transactional
    public ImportResult importCurrencies(Portfolio portfolio, List<CurrencyTrade> trades) {
        int inserted = 0, skipped = 0;
        List<String> warnings = new ArrayList<>();

        for (CurrencyTrade t : trades) {
            try {
                if (importOne(portfolio, t)) inserted++; else skipped++;
            } catch (RuntimeException e) {
                log.warn("Failed to import currencies trade {}: {}", t.number(), e.getMessage(), e);
                warnings.add("Currency trade " + t.number() + ": " + e.getMessage());
                skipped++;
            }
        }
        return new ImportResult(inserted, skipped, warnings);
    }

    private boolean importOne(Portfolio portfolio, CurrencyTrade t) {
        if (repository.existsByPortfolioIdAndSourceAndTradeNumber(
                portfolio.getId(), TradeSource.BROKER_REPORT, t.number())) {
            return false;
        }

        boolean buy  = isPositive(t.buyLotVolume());
        boolean sell = isPositive(t.sellLotVolume());
        if (buy == sell) {
            throw new ValidationException("Currency trade must be either BUY or SELL: " + t.number());
        }

        CreateCurrencyExchangeRequest req = new CreateCurrencyExchangeRequest(
                portfolio.getId(),
                DateParser.parseBrokerDate(t.date()),
                CurrencyParser.fromReport(t.lotCurrency()),
                CurrencyParser.fromReport(t.counterCurrency()),
                buy ? CurrencyTradeOperation.BUY : CurrencyTradeOperation.SELL,
                DecimalParser.parse(buy ? t.buyLotVolume() : t.sellLotVolume()),
                DecimalParser.parse(buy ? t.buyCounterVolume() : t.sellCounterVolume()),
                DecimalParser.parse(buy ? t.buyRate() : t.sellRate()),
                null,                  // comment
                t.number(),            // tradeNumber
                t.place(),             // platform
                TradeSource.BROKER_REPORT
        );

        currencyTradeService.exchange(req);
        return true;
    }

    private static boolean isPositive(String v) {
        BigDecimal d = DecimalParser.parse(v);
        return d != null && d.signum() > 0;
    }
}