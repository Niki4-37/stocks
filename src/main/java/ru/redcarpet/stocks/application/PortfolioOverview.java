package ru.redcarpet.stocks.application;

import ru.redcarpet.stocks.model.dto.PortfolioDetails;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public record PortfolioOverview(
        PortfolioDetails portfolio,// ← «сырые» данные (id, name, baseCurrency, balances, securities)
        Map<Currency, BigDecimal> cashByCurrency,      // деньги по валютам
        Map<Currency, BigDecimal> costBasisByCurrency, // себестоимость бумаг по валютам
        BigDecimal totalCostBasis,                     // в базовой валюте
        BigDecimal totalMarketValue,                   // null, пока нет цен
        BigDecimal totalCashValue,                     // в базовой валюте
        BigDecimal totalPortfolioValue                 // cash + market (или cash + costBasis, пока нет цен)
) {
    // удобные аксессоры для шаблона
    public Long id()              { return portfolio.id(); }
    public String name()          { return portfolio.name(); }
    public Currency baseCurrency(){ return portfolio.baseCurrency(); }
    public List<PortfolioDetails.Balance> balances() { return portfolio.balances(); }
    public List<PortfolioDetails.HeldSecurity> securities() { return portfolio.securities(); }
}