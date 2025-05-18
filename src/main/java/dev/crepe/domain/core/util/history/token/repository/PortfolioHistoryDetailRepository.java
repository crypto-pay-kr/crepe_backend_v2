package dev.crepe.domain.core.util.history.token.repository;

import dev.crepe.domain.core.util.history.token.model.entity.PortfolioHistoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioHistoryDetailRepository extends JpaRepository<PortfolioHistoryDetail, Long> {
}