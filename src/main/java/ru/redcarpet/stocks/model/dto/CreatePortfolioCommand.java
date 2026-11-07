package ru.redcarpet.stocks.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Currency;

public record CreatePortfolioCommand(
        @NotBlank @Size(max = 100) String name,
        @NotNull Currency baseCurrency,
        @PositiveOrZero BigDecimal initialBalance
) {
}
