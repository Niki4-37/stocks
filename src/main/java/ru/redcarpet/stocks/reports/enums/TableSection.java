package ru.redcarpet.stocks.reports.enums;

import java.util.Objects;

public enum TableSection {
    BLANK(""),
    CASH_OPERATION("1.1"),
    TOTAL_FEES_AND_PENALTIES("1.3"),
    TRADES("2.1");

    private final String marker;

    TableSection(String marker) {
        this.marker = marker;
    }

    public static TableSection byMarker(String normalized) {
        if (normalized == null || normalized.isEmpty()) return null;
        // сначала точное совпадение (быстрее и однозначнее)
        for (TableSection s : values()) {
            if (s.marker.equals(normalized)) return s;
        }
        // затем — по префиксу: "2.1.1" → TRADES (2.1)
        for (TableSection s : values()) {
            if (!s.marker.isEmpty() && normalized.startsWith(s.marker + ".")) {
                return s;
            }
        }
        return null;
    }
}
