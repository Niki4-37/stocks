package ru.redcarpet.stocks.reports.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.regex.Pattern;

public final class DateParser {

    private static final Pattern DD_MM_YY = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{2}");
    private static final DateTimeFormatter BROKER_DATE =
            DateTimeFormatter.ofPattern("dd.MM.yy");

    public static boolean isBrokerDate(String value) {
        return value != null && DD_MM_YY.matcher(value).matches();
    }

    public static LocalDate parseBrokerDate(String value) {
        return LocalDate.parse(value, BROKER_DATE);
    }

    public static LocalDate parseBrokerDateOrNull(String value) {
        if (Objects.isNull(value) || value.isBlank()) return null;
        try {
            return LocalDate.parse(value, BROKER_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
