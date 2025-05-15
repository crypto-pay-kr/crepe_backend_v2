package dev.crepe.domain.core.util.history.token.repository;

import dev.crepe.domain.core.util.history.token.model.entity.TokenPortfolioHistory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TokenHistoryRepository extends JpaRepository<TokenPortfolioHistory, Long> {



}
