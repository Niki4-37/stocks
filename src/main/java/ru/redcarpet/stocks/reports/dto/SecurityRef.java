package ru.redcarpet.stocks.reports.dto;

import org.apache.logging.log4j.util.Strings;

public record SecurityRef(
        String ticker,
        String isin,
        String name
) {
    public static final SecurityRef EMPTY = new SecurityRef(
            Strings.EMPTY,
            Strings.EMPTY,
            Strings.EMPTY);
}