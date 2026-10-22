package ru.redcarpet.stocks.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.redcarpet.stocks.model.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
