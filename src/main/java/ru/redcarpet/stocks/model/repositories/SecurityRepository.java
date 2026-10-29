package ru.redcarpet.stocks.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.redcarpet.stocks.model.entity.Security;

import java.util.List;
import java.util.Optional;

public interface SecurityRepository extends JpaRepository<Security, Long> {

    @Query("""
        select s from Security s
         where (:isin is not null and s.isin = :isin)
            or (:ticker is not null and s.ticker = :ticker
                and (:exchange is null or s.exchange = :exchange))
        """)
    List<Security> findByIsinOrTicker(@Param("isin") String isin,
                                      @Param("ticker") String ticker,
                                      @Param("exchange") String exchange);

    Optional<Security> findByTicker(String ticker);
    Optional<Security> findByTickerAndExchange(String ticker, String exchange);
    Optional<Security> findByIsin(String isin);
}
