package ru.redcarpet.stocks.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {

    List<CashOperation> findByPortfolioId(Long portfolioId);

    List<CashOperation> findByPortfolioIdAndSourceAndOperationDateBetween(
            Long portfolioId, TradeSource source, LocalDate from, LocalDate to);

    long countByPortfolioIdAndSourceAndOperationDateAndOperationTypeAndPlatformAndDirectionAndAmountAndCurrency(
            Long portfolioId, TradeSource source,
            LocalDate operationDate, CashOperationType operationType,
            String platform, CashFlowDirection direction, BigDecimal amount, Currency currency);
}
