package ru.redcarpet.stocks.ui.dto;

import java.time.Instant;
import java.util.Currency;

public record PortfolioResponse(
        Long id,
        String name,
        Currency baseCurrency,
        Instant createdAt
) {}