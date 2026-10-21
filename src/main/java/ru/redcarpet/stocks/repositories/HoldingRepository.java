package ru.redcarpet.stocks.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.redcarpet.stocks.entity.Holding;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findByPortfolioIdAndSecurityId(Long portfolioId, Long securityId);
    List<Holding> findByPortfolioId(Long id);
}
