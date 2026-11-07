package ru.redcarpet.stocks.model.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.redcarpet.stocks.model.entity.PortfolioBalance;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

public interface PortfolioBalanceRepository extends JpaRepository<PortfolioBalance, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select pb from PortfolioBalance pb
        where pb.portfolio.id = :portfolioId and pb.currency = :currency
        """)
    Optional<PortfolioBalance> findByPortfolioIdAndCurrencyForUpdate(
            @Param("portfolioId") Long portfolioId,
            @Param("currency") Currency currency);

    List<PortfolioBalance> findAllByPortfolioId(Long portfolioId);
    Optional<PortfolioBalance> findByPortfolioIdAndCurrency(Long portfolioId, Currency currency);
    void deleteByPortfolioId(Long portfolioId);
}
