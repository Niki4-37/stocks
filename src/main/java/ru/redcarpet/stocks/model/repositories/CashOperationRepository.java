package ru.redcarpet.stocks.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.redcarpet.stocks.model.dto.CurrencySum;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {

    List<CashOperation> findByPortfolioId(Long portfolioId);

    List<CashOperation> findByPortfolioIdAndSourceAndOperationDateBetween(
            Long portfolioId, TradeSource source, LocalDate from, LocalDate to);

    long countByPortfolioIdAndSourceAndOperationDateAndOperationTypeAndPlatformAndDirectionAndAmountAndCurrency(
            Long portfolioId, TradeSource source,
            LocalDate operationDate, CashOperationType operationType,
            String platform, CashFlowDirection direction, BigDecimal amount, Currency currency);

    List<CashOperation> findByPortfolioIdAndOperationDateBetweenOrderByOperationDateAsc(
            Long portfolioId, LocalDate from, LocalDate to);

    @Query("select min(op.operationDate) from CashOperation op where op.portfolio.id = :portfolioId")
    Optional<LocalDate> findMinOperationDateByPortfolioId(@Param("portfolioId") Long portfolioId);

    @Query("select max(op.operationDate) from CashOperation op where op.portfolio.id = :portfolioId")
    Optional<LocalDate> findMaxOperationDateByPortfolioId(@Param("portfolioId") Long portfolioId);

    @Query("""
        select op.currency as currency,
               coalesce(sum(case when op.direction = 'INCOME' then op.amount else -op.amount end), 0) as amount
          from CashOperation op
         where op.portfolio.id = :portfolioId
           and op.operationDate < :date
         group by op.currency
        """)
    List<CurrencySum> sumByCurrencyBeforeDate(@Param("portfolioId") Long portfolioId,
                                              @Param("date") LocalDate date);
}
