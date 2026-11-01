package ru.redcarpet.stocks.model.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record HoldingResponse(
        String ticker,
        String name,
        BigDecimal quantity,
        BigDecimal avgPrice,
        Currency currency,
        BigDecimal totalDividends,
        BigDecimal totalCoupons,
        BigDecimal realizedPnl,

        BigDecimal costBasis,          // quantity * avgPrice

        // пока null — заполнится, когда появится источник цен
        BigDecimal currentPrice,       // цена на текущий момент
        BigDecimal marketValue
) {
}
