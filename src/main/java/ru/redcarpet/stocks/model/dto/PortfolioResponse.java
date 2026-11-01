package ru.redcarpet.stocks.model.dto;

import java.time.Instant;
import java.util.Currency;

public record PortfolioResponse(
        Long id,
        String name,
        Currency baseCurrency,
        Instant createdAt
) {}