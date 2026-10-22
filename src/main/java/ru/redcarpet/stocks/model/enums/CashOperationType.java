package ru.redcarpet.stocks.model.enums;

public enum CashOperationType {
    DEPOSIT("Пополнение",             Direction.IN,   false),
    WITHDRAWAL("Вывод средств",       Direction.OUT,  false),
    FEE("Комиссия",                   Direction.OUT,  false),
    CURRENCY_EXCHANGE("Обмен валюты", Direction.OUT,  true);

    private final String displayName;
    private final Direction direction;
    private final boolean exchange;

    CashOperationType(String displayName, Direction direction, boolean exchange) {
        this.displayName = displayName;
        this.direction = direction;
        this.exchange = exchange;
    }

    public String getDisplayName() { return displayName; }
    public Direction getDirection() { return direction; }
    public boolean isExchange()    { return exchange; }

    public enum Direction {
        IN, OUT
    }
}
