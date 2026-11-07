package ru.redcarpet.stocks.importing.dto;

import ru.redcarpet.stocks.model.entity.CashOperation;

import java.util.List;

public record CashImportResult(
        ImportResult result,
        List<CashOperation> savedDividends
) {
    public static CashImportResult empty() { return new CashImportResult(ImportResult.empty(), List.of()); }
}