package ru.redcarpet.stocks.enums;

public enum OperationType {
    BUY("Покупка"),
    SELL("Продажа"),
    DIVIDEND("Дивиденды"),
    COUPON("Купон"),
    FEE("Комиссия"),
    DEPOSIT("Пополнение"),
    WITHDRAWAL("Вывод средств"),
    TRANSFER_IN("Зачисление ЦБ"),
    TRANSFER_OUT("Списание ЦБ");

    private final String displayName;

    OperationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    /** Влияет ли операция на количество бумаг в портфеле */
    public boolean affectsQuantity() {
        return this == BUY || this == SELL
                || this == TRANSFER_IN || this == TRANSFER_OUT;
    }

    /** Влияет ли на денежный баланс */
    public boolean affectsCash() {
        return this != TRANSFER_IN && this != TRANSFER_OUT;
    }
}
