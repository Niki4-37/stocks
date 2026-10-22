package ru.redcarpet.stocks.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.redcarpet.stocks.model.entity.Security;

import java.util.Optional;

public interface SecurityRepository extends JpaRepository<Security, Long> {

    Optional<Security> findByTicker(String ticker);
}
