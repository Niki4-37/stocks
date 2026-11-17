package ru.redcarpet.stocks.history;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public record DailyBalance(
        LocalDate date,
        Map<Currency, BigDecimal> cash,               // деньги по валютам
        List<Position> positions,                     // бумаги на этот день (по себестоимости)
        BigDecimal totalCashInBase,                   // деньги в базовой валюте (по курсу ЦБ на дату)
        BigDecimal totalPositionsCostBasis,           // себестоимость бумаг в базовой валюте (по курсу ЦБ на дату)
        BigDecimal totalValue                         // totalCashInBase + totalPositionsCostBasis
) {
    public record Position(
            Long securityId,
            String ticker,
            String name,
            BigDecimal quantity,
            BigDecimal avgPrice,
            Currency currency,
            BigDecimal costBasis                       // quantity × avgPrice
    ) {}
}