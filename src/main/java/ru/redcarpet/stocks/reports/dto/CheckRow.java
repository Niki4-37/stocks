package ru.redcarpet.stocks.reports.dto;

import java.math.BigDecimal;

public record CheckRow(
        String operation,     // "Вознаграждение компании" и т.п.
        String platform,       // площадка из 1.3 (может быть пустой = итог по всем)
        BigDecimal detailSum,  // сумма из детальных строк
        BigDecimal totalSum,   // сумма из раздела 1.3
        boolean match
) {
}
