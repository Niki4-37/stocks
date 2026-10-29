package ru.redcarpet.stocks.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.redcarpet.stocks.model.entity.Event;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
        select e
        from Event e
        where e.portfolio.id = :portfolioId
            and e.tradeSource = :source
            and e.tradeNumber in :tradeNumbers
    """)
    List<Event> findByTradeNumbers(
            @Param("portfolioId") Long portfolioId,
            @Param("source") TradeSource source,
            @Param("tradeNumbers") Collection<String> tradeNumbers);

    @Query("""
        select count(e) > 0 from Event e
         where e.portfolio.id = :portfolioId
           and e.tradeSource = :source
           and e.tradeNumber = :number
           and e.tradeDate = :date
           and e.tradeIsin = :isin
        """)
    boolean existsByTradeKey(@Param("portfolioId") Long portfolioId,
                             @Param("source") TradeSource source,
                             @Param("number") String number,
                             @Param("date") LocalDate date,
                             @Param("isin") String isin);
}
