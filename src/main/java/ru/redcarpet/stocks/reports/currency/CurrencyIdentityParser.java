package ru.redcarpet.stocks.reports.currency;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.common.RowValues;
import ru.redcarpet.stocks.reports.dto.CurrencyIdentityFromReport;

import java.util.Objects;

/** Распознаёт строку-идентификатор валютной подтаблицы: EURRUB_TOM | Валюта лота: | EUR | Сопряж. валюта: | Рубль. */
public final class CurrencyIdentityParser {

    private static final String LOT_LABEL = "Валюта лота:";
    private static final String COUNTER_LABEL = "Сопряж. валюта:";

    private CurrencyIdentityParser() {
    }

    public static CurrencyIdentityFromReport tryParse(Row row, DataFormatter fmt) {
        if (Objects.isNull(row)) return null;

        String instrument = RowValues.firstNonBlank(row, fmt);
        if (instrument.isBlank()) return null;

        String lot = RowValues.valueAfterLabel(row, fmt, LOT_LABEL);
        String counter = RowValues.valueAfterLabel(row, fmt, COUNTER_LABEL);
        if (lot.isBlank() && counter.isBlank()) return null;

        return new CurrencyIdentityFromReport(instrument, lot, counter);
    }
}