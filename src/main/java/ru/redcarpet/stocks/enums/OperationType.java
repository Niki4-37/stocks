package ru.redcarpet.stocks.enums;

import lombok.Getter;

@Getter
public enum OperationType {
    BUY("Покупка",                  Direction.OUT, Direction.IN),
    SELL("Продажа",                 Direction.IN, Direction.OUT),
    DIVIDEND("Дивиденды",           Direction.IN, Direction.NONE),
    COUPON("Купон",                 Direction.IN, Direction.NONE),
    TRANSFER_IN("Зачисление ЦБ",    Direction.NONE, Direction.IN),
    TRANSFER_OUT("Списание ЦБ",     Direction.NONE, Direction.OUT);

    private final String displayName;
    private final Direction cashDir;
    private final Direction securitiesDir;

    OperationType(String displayName, Direction cashDir, Direction securitiesDir) {
        this.displayName = displayName;
        this.cashDir = cashDir;
        this.securitiesDir = securitiesDir;
    }

    /** Влияет ли операция на количество бумаг в портфеле */
    public boolean affectsQuantity() {
        return securitiesDir != Direction.NONE;
    }

    /** Влияет ли на денежный баланс */
    public boolean affectsCash() {
        return cashDir != Direction.NONE;
    }

    public enum Direction {
        IN, OUT, NONE;

        public int sign() {
            return switch(this) {
                case IN -> 1;
                case OUT -> -1;
                case NONE -> 0;
            };
        }
    }
}
