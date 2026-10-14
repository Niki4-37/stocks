package ru.redcarpet.stocks.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.redcarpet.stocks.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
