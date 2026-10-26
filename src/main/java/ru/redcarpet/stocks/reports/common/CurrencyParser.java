package ru.redcarpet.stocks.reports.common;

import ru.redcarpet.stocks.model.exception.ValidationException;

import java.util.Currency;

/** Парсинг валюты из отчёта БКС. */
public final class CurrencyParser {

    private CurrencyParser() {}

    /** "Рубль" → RUB, "USD" → USD, "EUR" → EUR; иначе ValidationException. */
    public static Currency fromReport(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Currency is blank");
        }
        String code = switch (value.trim()) {
            case "Рубль", "RUB", "руб." -> "RUB";
            case "USD", "$"             -> "USD";
            case "EUR", "€"             -> "EUR";
            default -> value.trim();   // предполагаем ISO-код
        };
        try {
            return Currency.getInstance(code);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown currency: " + value);
        }
    }
}