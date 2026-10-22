package ru.redcarpet.stocks.reports.utils;

import java.util.Set;

public final class RowOperationNames {

    private static final Set<String> OPERATIONS = Set.of(
            "Урегулирование сделок",
            "Вознаграждение компании",
            "Фиксированное вознаграждение по тарифу"
    );

    private RowOperationNames() {}

    public static boolean contains(String op) {
        return op != null && OPERATIONS.contains(op);
    }
}