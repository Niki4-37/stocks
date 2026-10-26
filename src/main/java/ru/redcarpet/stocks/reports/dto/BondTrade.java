package ru.redcarpet.stocks.reports.dto;

/**
 * Сделка по облигации (подраздел «Облигация» раздела 2.1).
 */
public record BondTrade(
        String ticker,          // код бумаги (RU000A103HG0) из столбца A
        String isin,            // ISIN
        String securityName,    // эмитент
        String date,            // Дата
        String number,          // Номер
        String boughtQty,       // Куплено, шт
        String buyPrice,        // Цена, % (первая)
        String paymentAmount,   // Сумма платежа
        String buyAccrued,      // НКД Покупки
        String soldQty,         // Продано, шт
        String sellPrice,       // Цена, % (вторая)
        String proceeds,        // Сумма выручки
        String sellAccrued,     // НКД Продажи
        String currency,        // Валюта
        String deliveryFact,    // Поставка (факт)
        String place            // Место сделки (ММВБ и т.п.)
) {}