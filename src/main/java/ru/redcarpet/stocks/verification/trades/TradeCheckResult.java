package ru.redcarpet.stocks.verification.trades;

import java.time.LocalDate;
import java.util.List;

public record TradeCheckResult(
        String tradeNumber,
        LocalDate tradeDate,
        String tradeIsin,
        TradeMatchStatus status,          // MATCHED / MISSING / CONFLICT
        Long eventId,                      // null, если MISSING
        List<String> differences           // пусто, если MATCHED/MISSING
) {}