package ru.redcarpet.stocks.verification.trades;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TradeKeyDto(
        @NotBlank String tradeNumber,
        @NotNull LocalDate tradeDate,
        @NotBlank String tradeIsin
) {}