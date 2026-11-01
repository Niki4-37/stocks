package ru.redcarpet.stocks.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.redcarpet.stocks.model.entity.CurrencyTradeEntity;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.time.LocalDate;
import java.util.List;

public interface CurrencyTradeRepository extends JpaRepository<CurrencyTradeEntity, Long> {

    boolean existsByPortfolioIdAndSourceAndTradeNumber(
            Long portfolioId, TradeSource source, String tradeNumber);

    List<CurrencyTradeEntity> findByPortfolioIdAndTradeDateBetween(
            Long portfolioId, LocalDate from, LocalDate to);
}
