package ru.redcarpet.stocks.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.redcarpet.stocks.entity.CashOperation;

public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {
}
