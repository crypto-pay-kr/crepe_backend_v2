package dev.crepe.domain.core.util.history.token.repository;

import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TokenHistoryRepository extends JpaRepository<TokenHistory, Long> {



}
