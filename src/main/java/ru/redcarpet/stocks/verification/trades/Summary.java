package ru.redcarpet.stocks.verification.trades;

public record Summary(
        int total,
        int matched,
        int missing,
        int conflict
) {}
