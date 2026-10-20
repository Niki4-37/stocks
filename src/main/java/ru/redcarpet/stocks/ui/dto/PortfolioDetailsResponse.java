package ru.redcarpet.stocks.ui.dto;

import ru.redcarpet.stocks.dto.BalanceResponse;

import java.time.Instant;
import java.util.Currency;
import java.util.List;

public record PortfolioDetailsResponse(
        Long id,
        String name,
        Currency baseCurrency,
        Instant createdAt,
        List<BalanceResponse> balances,
        List<HoldingResponse> holdings
) {}
