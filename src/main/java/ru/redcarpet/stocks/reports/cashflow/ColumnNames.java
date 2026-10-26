package ru.redcarpet.stocks.reports.cashflow;

import java.util.List;

/** Имена (и их синонимы) столбцов отчёта БКС. */
public final class ColumnNames {

    private ColumnNames() {}

    public static final List<String> DATE = List.of("Дата");

    public static final List<String> OPERATION = List.of(
            "Операция",
            "Вид сбора/штрафа",
            "Начисленная комиссия"
    );

    public static final List<String> INCOME = List.of(
            "Сумма зачисления"
    );

    public static final List<String> EXPENSE = List.of(
            "Сумма списания",
            "Начисленная комиссия"
    );

    public static final List<String> PLATFORM = List.of("Площадка");

    public static final List<String> NOTE = List.of("Примечание");
}