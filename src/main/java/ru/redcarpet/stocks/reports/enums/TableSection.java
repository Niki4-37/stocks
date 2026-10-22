package ru.redcarpet.stocks.reports.enums;

public enum TableSection {
    BEFORE(""),
    DETAILS("1.1."),
    TOTALS_13("1.3."),
    AFTER("2.");

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
