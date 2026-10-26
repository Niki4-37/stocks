package ru.redcarpet.stocks.verification.cash;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.util.List;

public record CashCheckRequest(
        @NotNull Long portfolioId,
        @NotNull TradeSource source,
        @NotEmpty List<CashKeyDto> operations
) {
}
