package ru.redcarpet.stocks.verification.cash;

public record CashCheckResult(
        CashKeyDto key,
        CashMatchStatus status
) {
}
