package ru.redcarpet.stocks.reports.cashflow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Разбор поля «Примечание» у дивидендов. */
public record DividendNote(
        String issuer,
        String isin,
        String recordDate,   // дата фиксации
        String tax           // "налог $0,52" / "налог 740,00 руб."
) {

    private static final Pattern ISIN_PATTERN =
            Pattern.compile("\\b([A-Z]{2}[A-Z0-9]{9,10})\\b");

    private static final Pattern RECORD_DATE_PATTERN =
            Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})");

    private static final Pattern TAX_PATTERN =
            Pattern.compile("налог\\s+([^,]+)", Pattern.CASE_INSENSITIVE);

    public static DividendNote parse(String note) {
        if (note == null || note.isBlank()) return null;

        String isin = find(ISIN_PATTERN, note);
        String date = find(RECORD_DATE_PATTERN, note);
        String tax  = find(TAX_PATTERN, note);
        String issuer = extractIssuer(note);

        return new DividendNote(issuer, isin, date, tax);
    }

    /** Эмитент — часть до ", <год> г." (с учётом запятых внутри кавычек). */
    private static String extractIssuer(String note) {
        // "..., 2022 г., АО, ..." — берём всё до ", 2022 г."
        Matcher m = Pattern.compile("^(.+?),\\s*\\d{4}\\s*г\\.").matcher(note);
        return m.find() ? m.group(1).trim() : "";
    }

    private static String find(Pattern p, String s) {
        Matcher m = p.matcher(s);
        return m.find() ? m.group(1).trim() : "";
    }
}