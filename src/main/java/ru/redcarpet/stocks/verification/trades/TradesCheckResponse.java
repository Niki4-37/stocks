package ru.redcarpet.stocks.verification.trades;

import java.util.List;

public record TradesCheckResponse(
        Long portfolioId,
        List<TradeCheckResult> results,
        Summary summary
) {}