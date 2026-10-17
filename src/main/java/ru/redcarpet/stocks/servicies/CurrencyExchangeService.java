package ru.redcarpet.stocks.servicies;

import ru.redcarpet.stocks.dto.CreateCurrencyExchangeRequest;

public interface CurrencyExchangeService {
    void exchange(CreateCurrencyExchangeRequest request);
}
