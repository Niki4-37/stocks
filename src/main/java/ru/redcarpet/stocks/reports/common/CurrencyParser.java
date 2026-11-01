package ru.redcarpet.stocks.reports.common;

import ru.redcarpet.stocks.model.exception.ValidationException;

import java.util.Currency;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Парсинг валюты из отчёта БКС. */
public final class CurrencyParser {

    private static final Set<String> AVAILABLE_CURRENCIES =
            Currency.getAvailableCurrencies().stream()
                    .map(Currency::getCurrencyCode)
                    .collect(Collectors.toUnmodifiableSet());

    private CurrencyParser() {}

    public static Currency fromReport(String value) {
        return tryFromReport(value).orElseThrow(() ->
                new ValidationException("Unknown currency: " + value));
    }

    /** "Рубль" → RUB, "USD" → USD, "EUR" → EUR; иначе ValidationException. */
    public static Optional<Currency> tryFromReport(String value) {
        if (value == null || value.isBlank()) return Optional.empty();
        String code = switch (value.trim()) {
            case "Рубль", "RUB", "руб." -> "RUB";
            case "USD", "$"             -> "USD";
            case "EUR", "€"             -> "EUR";
            default -> value.trim();
        };
        if (!AVAILABLE_CURRENCIES.contains(code)) return Optional.empty();
        return Optional.of(Currency.getInstance(code));
    }
}