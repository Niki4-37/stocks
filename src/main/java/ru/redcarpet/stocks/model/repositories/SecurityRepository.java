package ru.redcarpet.stocks.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.redcarpet.stocks.model.entity.Security;

import java.util.List;
import java.util.Optional;

public interface SecurityRepository extends JpaRepository<Security, Long> {

    @Query(value = """
        select * from stocks.securities s
         where stocks.normalize_security_name(s.name) = stocks.normalize_security_name(:issuer)
        """, nativeQuery = true)
    List<Security> findByNormalizedName(@Param("issuer") String issuer);

    Optional<Security> findByTicker(String ticker);
    Optional<Security> findByTickerAndExchange(String ticker, String exchange);
    Optional<Security> findByIsin(String isin);
}
