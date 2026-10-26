package ru.redcarpet.stocks.reports.cashflow;

import java.math.BigDecimal;
import java.util.Objects;

public final class DecimalParser {
    private DecimalParser() {}

    public static BigDecimal parse(String s) {
        if (Objects.isNull(s)) return null;
        String cleaned = s.replace('\u00A0', ' ')
                .replace(" ", "")
                .replace(",", ".");
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}