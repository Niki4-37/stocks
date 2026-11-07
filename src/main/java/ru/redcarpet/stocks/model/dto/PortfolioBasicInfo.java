package ru.redcarpet.stocks.model.dto;

import java.time.Instant;
import java.util.Currency;

public record PortfolioBasicInfo(
        Long id,
        String name,
        Currency baseCurrency,
        Instant createdAt
) {}