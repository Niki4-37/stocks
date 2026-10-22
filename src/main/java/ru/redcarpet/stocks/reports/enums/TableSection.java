package ru.redcarpet.stocks.reports.enums;

public enum TableSection {
    BLANK(""),
    CASH_OPERATION("1.1."),
    TOTAL_FEES_AND_PENALTIES("1.3."),
    TRADES("2.");

    private final String value;

    TableSection(String value) {
        this.value = value;
    }

    public static TableSection byMarker(String marker) {
        if (marker == null) return null;
        for (TableSection s : values()) {
            if (s.value.equals(marker)) return s;
        }
        return null;
    }
}
