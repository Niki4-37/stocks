package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.dto.CreateCashOperationRequest;

public interface CashOperationService {
    void create(CreateCashOperationRequest request);
}
