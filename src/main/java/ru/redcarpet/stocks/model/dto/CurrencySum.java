package ru.redcarpet.stocks.model.dto;

import java.math.BigDecimal;
import java.util.Currency;

public interface CurrencySum {
    Currency getCurrency();
    BigDecimal getAmount();
}