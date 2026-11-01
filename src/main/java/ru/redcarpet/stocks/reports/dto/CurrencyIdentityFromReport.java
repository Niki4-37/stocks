package ru.redcarpet.stocks.reports.dto;

public record CurrencyIdentityFromReport(
        String instrument,       // EURRUB_TOM
        String lotCurrency,      // EUR
        String counterCurrency   // Рубль
) {
    public static final CurrencyIdentityFromReport EMPTY = new CurrencyIdentityFromReport("", "", "");
}