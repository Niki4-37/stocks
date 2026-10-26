package ru.redcarpet.stocks.reports.enums;

import java.util.Arrays;
import java.util.Optional;

/** Реестр операций отчёта БКС и их признаков. */
public enum RowOperationNames {

//    OVERNIGHT_LOAN("Займы \"овернайт\"",        Flow.ANY, false),
    SETTLEMENT("Урегулирование сделок",         Flow.EXPENSE, false),
    COMPANY_FEE("Вознаграждение компании",      Flow.EXPENSE, false),
    FIXED_FEE("Фиксированное вознаграждение по тарифу", Flow.EXPENSE, false),
    NDK_COUPON("НКД от операций",               Flow.ANY, false),
    DIVIDENDS("Дивиденды",                      Flow.INCOME,  true),
    COUPON("Погашение купона",                  Flow.INCOME,  true),
    CASH_INCOME("Приход ДС",                    Flow.INCOME,  false);

    public enum Flow { INCOME, EXPENSE, ANY }

    private final String title;
    private final Flow flow;
    private final boolean hasNote;

    RowOperationNames(String title, Flow flow, boolean hasNote) {
        this.title = title;
        this.flow = flow;
        this.hasNote = hasNote;
    }

    public String title() { return title; }
    public Flow flow() { return flow; }
    public boolean hasNote() { return hasNote; }

    /** Найти операцию по названию. */
    public static Optional<RowOperationNames> byTitle(String title) {
        if (title == null) return Optional.empty();
        String t = title.trim();
        return Arrays.stream(values())
                .filter(op -> op.title.equalsIgnoreCase(t))
                .findFirst();
    }

    /** Есть ли такая операция в реестре. */
    public static boolean contains(String title) {
        return byTitle(title).isPresent();
    }

    /** Есть ли у операции примечание, которое нужно разбирать. */
    public static boolean hasNote(String title) {
        return byTitle(title).map(RowOperationNames::hasNote).orElse(false);
    }

    public static boolean isExpense(String title) {
        return byTitle(title).map(op -> op.flow() == Flow.EXPENSE).orElse(false);
    }

    public static boolean isIncome(String title) {
        return byTitle(title).map(op -> op.flow() == Flow.INCOME).orElse(false);
    }
}