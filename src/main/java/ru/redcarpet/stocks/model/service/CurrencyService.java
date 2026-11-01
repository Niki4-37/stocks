package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.CreateCurrencyExchangeRequest;
import ru.redcarpet.stocks.model.entity.CurrencyTradeEntity;

public interface CurrencyService {
    CurrencyTradeEntity exchange(CreateCurrencyExchangeRequest request);
}
