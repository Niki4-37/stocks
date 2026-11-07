package ru.redcarpet.stocks.importing.dto;

import ru.redcarpet.stocks.model.dto.BalanceMismatch;

import java.util.ArrayList;
import java.util.List;

public record ImportSummarizedResult(
        ImportResult cash,
        ImportResult trades,
        ImportResult dividends,
        ImportResult currencies,
        List<BalanceMismatch> balanceMismatches
) {
    public int totalInserted() {
        return cash.inserted() + trades.inserted()
                + dividends.inserted() + currencies.inserted();
    }

    public boolean hasBalanceMismatches() {
        return balanceMismatches != null && !balanceMismatches.isEmpty();
    }

    public List<String> warnings() {
        List<String> all = new ArrayList<>(cash.warnings());
        all.addAll(trades.warnings());
        all.addAll(dividends.warnings());
        all.addAll(currencies.warnings());
        return all;
    }
}