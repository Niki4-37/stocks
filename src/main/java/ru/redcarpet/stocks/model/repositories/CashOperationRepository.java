package ru.redcarpet.stocks.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.redcarpet.stocks.model.entity.CashOperation;

public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {
}
