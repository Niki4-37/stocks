package ru.redcarpet.stocks.model.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public enum CashOperationType {
    DEPOSIT("Пополнение",                   CashFlowDirection.INCOME),
    WITHDRAWAL("Вывод средств",             CashFlowDirection.EXPENSE),
    FEE("Комиссия",                         CashFlowDirection.EXPENSE, Feature.CHECKING),
    DIVIDEND("Дивиденды",                   CashFlowDirection.INCOME, Feature.DIVIDEND),
    COUPON("Погашение купона",              CashFlowDirection.INCOME,  Feature.DIVIDEND),
    CASH_INCOME("Приход ДС",                CashFlowDirection.INCOME),
    CASH_EXPENSE("Вывод ДС",                CashFlowDirection.EXPENSE),
    SETTLEMENT("Урегулирование сделок",     CashFlowDirection.EXPENSE, Feature.CHECKING),
    COMPANY_FEE("Вознаграждение компании",  CashFlowDirection.EXPENSE, Feature.CHECKING),
    FIXED_FEE("Фиксированное вознаграждение по тарифу", CashFlowDirection.EXPENSE, Feature.CHECKING),
    ACCRUED_INTEREST("НКД от операций",     CashFlowDirection.EXPENSE),
    INTEREST("Проценты по займам \"овернайт\"", CashFlowDirection.INCOME),
    TAX("НДФЛ",                             CashFlowDirection.EXPENSE),
    BUY_SELL("Покупка/Продажа",     null),
    DEPO_FEE("Вознаграждение за обслуживание счета депо", CashFlowDirection.EXPENSE, Feature.CHECKING),
    WITHDRAWAL_FEE("Оплата за вывод денежных средств", CashFlowDirection.EXPENSE, Feature.CHECKING);

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
    public boolean hasDividendFeature() { return has(Feature.DIVIDEND); }
    public boolean hasCheckingFeature() { return has(Feature.CHECKING); }

    public static Optional<CashOperationType> byDisplayName(String title) {
        if (Objects.isNull(title)) return Optional.empty();
        String t = title.trim();
        return Arrays.stream(values())
                .filter(op -> op.displayName.equalsIgnoreCase(t))
                .findFirst();
    }

    public enum Feature {
        DIVIDEND, CHECKING
    }
}
