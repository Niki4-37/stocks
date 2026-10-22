package ru.redcarpet.stocks.reports.utils;

import java.util.List;

/** Имена (и их синонимы) столбцов отчёта БКС. */
public final class ColumnNames {

    private ColumnNames() {}

    public static final List<String> OPERATION = List.of(
            "Операция",
            "Вид сбора/штрафа",
            "Начисленная комиссия"
    );

    public static final List<String> DATE = List.of("Дата");

    public static final List<String> AMOUNT = List.of(
            "Сумма списания",       // детальная 1.1
            "Начисленная комиссия"  // итоговая 1.3 (новый формат)
    );

    public static final List<String> PLATFORM = List.of("Площадка");
}