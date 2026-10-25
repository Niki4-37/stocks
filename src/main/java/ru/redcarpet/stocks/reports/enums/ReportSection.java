package ru.redcarpet.stocks.reports.enums;

public enum ReportSection {
    NONE(""),
    CASH_OPERATION("1.1"),
    TOTAL_FEES_AND_PENALTIES("1.3"),
    TRADES("2.1");

    private final String marker;

    ReportSection(String marker) {
        this.marker = marker;
    }

    public static ReportSection byMarker(String normalized) {
        if (normalized == null || normalized.isEmpty()) return null;
        // сначала точное совпадение (быстрее и однозначнее)
        for (ReportSection s : values()) {
            if (s.marker.equals(normalized)) return s;
        }
        // затем — по префиксу: "2.1.1" → TRADES (2.1)
        for (ReportSection s : values()) {
            if (!s.marker.isEmpty() && normalized.startsWith(s.marker + ".")) {
                return s;
            }
        }
        return null;
    }
}
