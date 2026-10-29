package ru.redcarpet.stocks.model.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.entity.Security;
import ru.redcarpet.stocks.model.enums.OperationType;
import ru.redcarpet.stocks.model.enums.SecurityType;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.service.SecurityResolver;
import ru.redcarpet.stocks.reports.common.CurrencyParser;
import ru.redcarpet.stocks.reports.common.DateParser;
import ru.redcarpet.stocks.reports.common.DecimalParser;
import ru.redcarpet.stocks.reports.dto.BondTrade;
import ru.redcarpet.stocks.reports.dto.FundTrade;
import ru.redcarpet.stocks.reports.dto.SecurityIdentityFromReport;
import ru.redcarpet.stocks.reports.dto.StockTrade;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class EventFactory {

    private final SecurityResolver securityResolver;

    public Event fromStockTrade(StockTrade t, Portfolio portfolio) {
        return fromTrade(
                portfolio,
                new SecurityIdentityFromReport(t.ticker(), t.isin(), t.securityName()),
                SecurityType.STOCK,
                mapExchange(t.place()),
                t.date(), t.number(), t.isin(),
                t.boughtQty(), t.buyPrice(), t.paymentAmount(),
                t.soldQty(), t.sellPrice(), t.proceeds(),
                t.currency()
        );
    }

    public Event fromFundTrade(FundTrade t, Portfolio portfolio) {
        return fromTrade(
                portfolio,
                new SecurityIdentityFromReport(t.ticker(), t.isin(), t.securityName()),
                SecurityType.ETF,
                mapExchange(t.place()),
                t.date(), t.number(), t.isin(),
                t.boughtQty(), t.buyPrice(), t.paymentAmount(),
                t.soldQty(), t.sellPrice(), t.proceeds(),
                t.currency()
        );
    }

    public Event fromBondTrade(BondTrade t, Portfolio portfolio) {
        return fromTrade(
                portfolio,
                new SecurityIdentityFromReport(t.ticker(), t.isin(), t.securityName()),
                SecurityType.BOND,
                mapExchange(t.place()),
                t.date(), t.number(), t.isin(),
                t.boughtQty(), t.buyPrice(), t.paymentAmount(),
                t.soldQty(), t.sellPrice(), t.proceeds(),
                t.currency()
        );
    }

    private Event fromTrade(Portfolio portfolio,
                            SecurityIdentityFromReport ref, SecurityType type, String exchange,
                            String date, String tradeNumber, String tradeIsin,
                            String boughtQty, String buyPrice, String paymentAmount,
                            String soldQty, String sellPrice, String proceeds,
                            String currency) {
        boolean buy = isPositive(boughtQty);
        boolean sell = isPositive(soldQty);
        if (buy == sell) {
            throw new ValidationException(
                    "Trade must be either BUY or SELL: " + tradeNumber);
        }

        Security security = securityResolver.resolve(ref, type, exchange);

        Event e = new Event();
        e.setPortfolio(portfolio);
        e.setSecurity(security);
        e.setOperationType(buy ? OperationType.BUY : OperationType.SELL);
        e.setEventDate(DateParser.parseBrokerDate(date));
        e.setPrice(DecimalParser.parse(buy ? buyPrice : sellPrice));
        e.setQuantity(DecimalParser.parse(buy ? boughtQty : soldQty));
        e.setCurrency(CurrencyParser.fromReport(currency));
        e.setAmount(DecimalParser.parse(buy ? paymentAmount : proceeds));
        e.setExchangeRate(null);

        // внешний ключ для дедупликации
        e.setTradeNumber(tradeNumber);
        e.setTradeDate(DateParser.parseBrokerDate(date));
        e.setTradeIsin(tradeIsin);
        e.setTradeSource(TradeSource.BROKER_REPORT);

        return e;
    }

    private static boolean isPositive(String v) {
        BigDecimal d = DecimalParser.parse(v);
        return d != null && d.signum() > 0;
    }

    private static String mapExchange(String place) {
        if (place == null || place.isBlank()) return null;
        return switch (place.trim()) {
            case "ММВБ", "МосБирж(ВР/ДМ)", "MOEX" -> "MOEX";
            case "СПБ", "SPB" -> "SPB";
            default -> place.trim();
        };
    }
}