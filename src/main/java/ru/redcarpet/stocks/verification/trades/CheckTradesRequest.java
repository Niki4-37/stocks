package ru.redcarpet.stocks.verification.trades;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckTradesRequest(
        @NotNull Long portfolioId,
        @NotNull TradeSource tradeSource,
        @NotEmpty List<TradeKeyDto> trades
) {}