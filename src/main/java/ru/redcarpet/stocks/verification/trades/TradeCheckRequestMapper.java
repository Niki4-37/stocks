package ru.redcarpet.stocks.verification.trades;

import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.reports.common.DateParser;
import ru.redcarpet.stocks.reports.dto.BondTrade;
import ru.redcarpet.stocks.reports.dto.CurrencyTrade;
import ru.redcarpet.stocks.reports.dto.FundTrade;
import ru.redcarpet.stocks.reports.dto.StockTrade;
import ru.redcarpet.stocks.reports.dto.TradesResult;

import java.util.ArrayList;
import java.util.List;

/** Преобразует результат парсинга отчёта в запрос сверки с БД. */
@Component
public class TradeCheckRequestMapper {

    public TradesCheckRequest toCheckRequest(Long portfolioId, TradesResult trades, List<CurrencyTrade> currencies) {
        List<TradeKeyDto> keys = new ArrayList<>();

        for (StockTrade t : trades.stocks()) {
            keys.add(new TradeKeyDto(t.number(), DateParser.parseBrokerDate(t.date()), t.isin()));
        }
        for (FundTrade t : trades.funds()) {
            keys.add(new TradeKeyDto(t.number(), DateParser.parseBrokerDate(t.date()), t.isin()));
        }
        for (BondTrade t : trades.bonds()) {
            keys.add(new TradeKeyDto(t.number(), DateParser.parseBrokerDate(t.date()), t.isin()));
        }
        // валюта: ISIN нет, но номер + дата достаточно
        for (CurrencyTrade t : currencies) {
            keys.add(new TradeKeyDto(t.number(), DateParser.parseBrokerDate(t.date()), ""));
        }

        return new TradesCheckRequest(portfolioId, TradeSource.BROKER_REPORT, keys);
    }
}