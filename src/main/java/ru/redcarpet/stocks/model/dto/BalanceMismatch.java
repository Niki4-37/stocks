package ru.redcarpet.stocks.model.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record BalanceMismatch(Currency currency, BigDecimal cached, BigDecimal expected) {
}