package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.CreateCurrencyExchangeRequest;

public interface CurrencyExchangeService {
    void exchange(CreateCurrencyExchangeRequest request);
}
