package ru.redcarpet.stocks.verification.trades;

import java.util.List;

public record CheckTradesResponse(
        Long portfolioId,
        List<TradeCheckResult> results,
        Summary summary
) {}