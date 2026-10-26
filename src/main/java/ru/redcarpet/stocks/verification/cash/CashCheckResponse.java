package ru.redcarpet.stocks.verification.cash;

import java.util.List;

public record CashCheckResponse(
        Long portfolioId,
        List<CashCheckResult> results,
        Summary summary
) {
}
