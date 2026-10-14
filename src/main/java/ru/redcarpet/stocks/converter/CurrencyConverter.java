package ru.redcarpet.stocks.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Currency;
import java.util.Objects;

@Converter(autoApply = true)
public class CurrencyConverter implements AttributeConverter<Currency, String> {

    @Override
    public String convertToDatabaseColumn(Currency currency) {
        return Objects.isNull(currency) ? null : currency.getCurrencyCode();
    }

    @Override
    public Currency convertToEntityAttribute(String code) {
        return Objects.isNull(code) ? null : Currency.getInstance(code);
    }
}
