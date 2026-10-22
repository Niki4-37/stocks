package ru.redcarpet.stocks.model.enums;

public enum SecurityType {
    STOCK("Акция", "Акции"),
    BOND("Облигация", "Облигации"),
    ETF("ETF", "ETF"),
    MUTUAL_FUND("ПИФ", "Паевые фонды"),
    FUTURES("Фьючерс", "Фьючерсы"),
    OPTION("Опцион", "Опционы"),
    CURRENCY("Валюта", "Валюты"),
    CRYPTO("Криптовалюта", "Криптовалюты"),
    INDEX("Индекс", "Индексы");

    private final String displayName;
    private final String pluralName;

    SecurityType(String displayName, String pluralName) {
        this.displayName = displayName;
        this.pluralName = pluralName;
    }

    public String getDisplayName() { return displayName; }
    public String getPluralName()  { return pluralName; }      // Индекс
}
