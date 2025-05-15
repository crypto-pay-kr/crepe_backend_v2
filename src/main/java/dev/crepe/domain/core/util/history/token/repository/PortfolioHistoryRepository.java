package dev.crepe.domain.core.util.history.token.repository;

import dev.crepe.domain.core.util.history.token.model.entity.PortfolioHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioHistoryRepository extends JpaRepository<PortfolioHistory, Long> {
}