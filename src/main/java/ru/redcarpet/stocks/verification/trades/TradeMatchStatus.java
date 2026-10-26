package ru.redcarpet.stocks.verification.trades;

public enum TradeMatchStatus {
    MATCHED,     // есть событие с тем же (tradeNumber, tradeDate, tradeIsin)
    MISSING,     // такого tradeNumber нет в БД
    CONFLICT     // tradeNumber есть, но tradeDate или tradeIsin не совпадают
}
