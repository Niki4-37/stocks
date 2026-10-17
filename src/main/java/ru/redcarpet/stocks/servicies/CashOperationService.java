package ru.redcarpet.stocks.servicies;

import ru.redcarpet.stocks.dto.CreateCashOperationRequest;

public interface CashOperationService {
    void create(CreateCashOperationRequest request);
}
