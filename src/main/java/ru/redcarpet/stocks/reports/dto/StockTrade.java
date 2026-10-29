package ru.redcarpet.stocks.reports.dto;

/**
 * Сделка по акции (подраздел «Акция» раздела 2.1).
 */
public record StockTrade(
        String ticker,
        String isin,
        String securityName,
        String date,          // Дата
        String number,        // Номер
        String boughtQty,     // Куплено, шт
        String buyPrice,      // первая «Цена»
        String paymentAmount, // Сумма платежа
        String soldQty,       // Продано, шт
        String sellPrice,     // вторая «Цена»
        String proceeds,      // Сумма выручки
        String currency,      // Валюта
        String deliveryFact,   // Поставка (факт)
        String place
) {}