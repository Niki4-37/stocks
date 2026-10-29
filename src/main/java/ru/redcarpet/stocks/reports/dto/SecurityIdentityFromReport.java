package ru.redcarpet.stocks.reports.dto;

import org.apache.logging.log4j.util.Strings;

public record SecurityIdentityFromReport(
        String ticker,
        String isin,
        String name
) {
    public static final SecurityIdentityFromReport EMPTY = new SecurityIdentityFromReport(
            Strings.EMPTY,
            Strings.EMPTY,
            Strings.EMPTY);
}