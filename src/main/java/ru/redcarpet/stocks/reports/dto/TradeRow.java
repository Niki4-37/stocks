package ru.redcarpet.stocks.reports.dto;

/**
 * Сделка из раздела 2.1 (по бумагам).
 * @param ticker       тикер бумаги (столбец A в строке-идентификаторе)
 * @param isin         ISIN бумаги (US0378331005 и т.п.), может быть пустым
 * @param regNumber    номер гос. регистрации (1-02-00028-A), может быть пустым
 * @param securityName название эмитента/бумаги (Apple Inc., ПАО "Газпром"), может быть пустым
 * @param date         дата сделки (dd.MM.yy)
 * @param number       номер сделки
 * @param boughtQty    куплено, шт
 * @param buyPrice     первая "Цена" (цена покупки)
 * @param paymentAmount сумма платежа
 * @param soldQty      продано, шт
 * @param sellPrice    вторая "Цена" (цена продажи)
 * @param proceeds     сумма выручки
 * @param currency     валюта
 * @param deliveryFact поставка (факт)
 */
public record TradeRow(
        String ticker,
        String isin,
        String regNumber,
        String securityName,
        String date,
        String number,
        String boughtQty,
        String buyPrice,
        String paymentAmount,
        String soldQty,
        String sellPrice,
        String proceeds,
        String currency,
        String deliveryFact
) {}
