package ru.redcarpet.stocks.reports.enums;

import java.util.Objects;

public enum TradeSubsection {
    NONE(""),
    STOCK("акция"),
    BOND("облигация"),
    FUND("пай"),
    CURRENCY("иностранная валюта"),
    OVERNIGHT_CB("займы \"овернайт цб\"");

    private final String normalizedTitle;

    TradeSubsection(String title) {
        this.normalizedTitle = normalizeStatic(title);
    }

    public String title() { return normalizedTitle; }

    public static TradeSubsection byTitle(String text) {
        String t = normalizeStatic(text);
        if (t.isEmpty()) return null;

        // 1) точное равенство нормализованных форм
        for (TradeSubsection s : values()) {
            if (!s.normalizedTitle.isEmpty() && s.normalizedTitle.equals(t)) return s;
        }
        // 2) префиксный fallback: строка начинается с названия подраздела
        //    (на случай "... Овернайт ЦБ" + лишний хвост/двоеточие/пояснение)
        for (TradeSubsection s : values()) {
            if (!s.normalizedTitle.isEmpty() && t.startsWith(s.normalizedTitle)) return s;
        }
        return null;
    }

    public boolean isTradable() {
        return this == STOCK || this == FUND || this == CURRENCY; // облигации/овернайт пока не обрабатываем
    }

    // тот же normalize, но static (enum-конструктор не может звать instance-методы до this)
    private static String normalizeStatic(String text) {
        if (text == null) return "";
        return text
                .trim()
                .toLowerCase()
                .replace('«', '"').replace('»', '"')
                .replace('“', '"').replace('”', '"')
                .replace('„', '"')
                .replaceAll("\\s+", " ")
                .replaceAll(":+$", "")
                .trim();
    }
}
