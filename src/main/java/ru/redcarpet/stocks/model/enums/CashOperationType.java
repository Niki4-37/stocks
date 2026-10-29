package ru.redcarpet.stocks.model.enums;

import ru.redcarpet.stocks.model.exception.ValidationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public enum CashOperationType {
    DEPOSIT("Пополнение",             CashFlowDirection.INCOME),
    WITHDRAWAL("Вывод средств",       CashFlowDirection.EXPENSE),
    FEE("Комиссия",                   CashFlowDirection.EXPENSE),
    CURRENCY_EXCHANGE("Обмен валюты", CashFlowDirection.EXPENSE, Feature.EXCHANGE),
    DIVIDEND("Дивиденды",             CashFlowDirection.INCOME, Feature.DIVIDEND),
    COUPON("Погашение купона",        CashFlowDirection.INCOME,  Feature.DIVIDEND),
    CASH_INCOME("Приход ДС",          CashFlowDirection.INCOME),
    SETTLEMENT("Урегулирование сделок", CashFlowDirection.EXPENSE),
    COMPANY_FEE("Вознаграждение компании",       CashFlowDirection.EXPENSE),
    FIXED_FEE("Фиксированное вознаграждение по тарифу", CashFlowDirection.EXPENSE),
    ACCRUED_INTEREST("НКД от операций", CashFlowDirection.EXPENSE),
    INTEREST("Проценты по займам \"овернайт\"", CashFlowDirection.INCOME);

    private final String displayName;
    private final CashFlowDirection direction;
    private final Set<Feature> features;

    CashOperationType(String displayName, CashFlowDirection direction, Feature... features) {
        this.displayName = displayName;
        this.direction = direction;
        this.features = EnumSet.noneOf(Feature.class);
        Collections.addAll(this.features, features);
    }

    public String getDisplayName() { return displayName; }
    public CashFlowDirection getDirection() { return direction; }

    public boolean has(Feature f) { return features.contains(f); }
    public boolean isDividend()   { return has(Feature.DIVIDEND); }
    public boolean isExchange()   { return has(Feature.EXCHANGE); }

    public static Optional<CashOperationType> byDisplayName(String title) {
        if (Objects.isNull(title)) return Optional.empty();
        String t = title.trim();
        return Arrays.stream(values())
                .filter(op -> op.displayName.equalsIgnoreCase(t))
                .findFirst();
    }

    public static CashOperationType requireByDisplayName(String title) {
        return byDisplayName(title).orElseThrow(() -> new ValidationException(
                "Unknown operation: " + title));
    }

    public static boolean isExpense(String title) {
        return byDisplayName(title)
                .map(op -> op.getDirection() == CashFlowDirection.EXPENSE)
                .orElse(false);   // ← это нормально: Optional<Boolean> → boolean
    }

    public static boolean isOvernightInterest(String title) {
        return byDisplayName(title)
                .map(op -> op == INTEREST)
                .orElse(false);
    }

    public enum Feature {
        DIVIDEND,
        EXCHANGE
    }
}
