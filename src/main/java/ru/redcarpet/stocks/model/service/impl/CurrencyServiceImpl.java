package ru.redcarpet.stocks.model.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.redcarpet.stocks.model.dto.CreateCurrencyExchangeRequest;
import ru.redcarpet.stocks.model.entity.CurrencyTradeEntity;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.repositories.CurrencyTradeRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;
import ru.redcarpet.stocks.model.service.BalanceService;
import ru.redcarpet.stocks.model.service.CurrencyService;

import java.math.BigDecimal;
import java.util.Currency;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyTradeRepository repository;
    private final PortfolioRepository portfolioRepository;
    private final BalanceService balanceService;

    @Override
    public CurrencyTradeEntity exchange(CreateCurrencyExchangeRequest request) {
        request.validate();

        Portfolio portfolio = portfolioRepository.findById(request.portfolioId())
                .orElseThrow(() -> new NotFoundException("Portfolio not found: " + request.portfolioId()));

        Currency lot     = request.lotCurrency();
        Currency counter = request.counterCurrency();

        BigDecimal rate = request.resolvedRate();
        BigDecimal lotVolume = request.resolvedLotVolume();
        BigDecimal counterVolume = request.resolvedCounterVolume();

        if (request.isManual()) {
            if (request.isBuying()) {
                balanceService.debit(portfolio, counter, counterVolume);
                balanceService.credit(portfolio, lot, lotVolume);
            } else {
                balanceService.debit(portfolio, lot, lotVolume);
                balanceService.credit(portfolio, counter, counterVolume);
            }
        }

        boolean isBuy = request.isBuying();

        // запись сделки
        CurrencyTradeEntity entity = new CurrencyTradeEntity();
        entity.setPortfolio(portfolio);
        entity.setInstrument(buildInstrument(lot, counter));   // "EURRUB_TOM"
        entity.setLotCurrency(lot);
        entity.setCounterCurrency(counter);
        entity.setTradeNumber(request.tradeNumber());
        entity.setTradeDate(request.operationDate());
        entity.setOperationType(request.operationType());
        entity.setRate(rate);
        entity.setLotVolume(isBuy ? request.counterVolume() : request.lotVolume());
        entity.setCounterVolume(isBuy ? request.lotVolume() : request.counterVolume());
        entity.setPlatform(request.platform());
        entity.setSource(request.source());

        log.info("Created CurrencyTradeEntity with: lot={}, counter={}, ",
                lot, counter);
        return repository.save(entity);
    }

    private static String buildInstrument(Currency lot, Currency counter) {
        // "EURRUB_TOM" — по кодам
        return lot.getCurrencyCode() + counter.getCurrencyCode() + "_TOM";
    }

}
