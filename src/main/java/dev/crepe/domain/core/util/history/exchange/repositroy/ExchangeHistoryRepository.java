package dev.crepe.domain.core.util.history.exchange.repositroy;

import dev.crepe.domain.core.util.history.exchange.model.entity.ExchangeHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExchangeHistoryRepository extends JpaRepository<ExchangeHistory, Long> {

   List<ExchangeHistory> findByFromAccount_IdInOrToAccount_IdIn(List<Long> fromIds, List<Long> toIds);
}
