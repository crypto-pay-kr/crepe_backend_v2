package dev.crepe.domain.core.util.history.exchange.repositroy;

import dev.crepe.domain.core.util.history.exchange.model.entity.ExchangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeHistoryRepository extends JpaRepository<ExchangeHistory, Long> {


}
