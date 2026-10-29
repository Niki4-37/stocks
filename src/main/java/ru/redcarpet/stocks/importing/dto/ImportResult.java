package ru.redcarpet.stocks.importing.dto;

import java.util.List;
import java.util.stream.Stream;

public record ImportResult(int inserted, int skipped, List<String> warnings) {

    public ImportResult {
        // защита от null и от мутабельного списка «снаружи»
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static ImportResult empty() { return new ImportResult(0, 0, List.of()); }

    public ImportResult merge(ImportResult other) {
        return new ImportResult(
                this.inserted + other.inserted(),
                this.skipped + other.skipped(),
                Stream.concat(this.warnings.stream(), other.warnings().stream()).toList());
    }
}