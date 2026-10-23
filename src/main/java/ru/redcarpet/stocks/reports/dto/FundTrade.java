package ru.redcarpet.stocks.reports.dto;

/** Сделка по паю (подраздел «Пай» раздела 2.1). */
public record FundTrade(
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