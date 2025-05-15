package dev.crepe.domain.core.util.history.subscribe.repository;

import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.util.history.subscribe.model.SubscribeHistoryType;
import dev.crepe.domain.core.util.history.subscribe.model.entity.SubscribeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SubscribeHistoryRepository extends JpaRepository<SubscribeHistory, Long> {

    @Query("""
        SELECT COALESCE(SUM(h.amount), 0) 
        FROM SubscribeHistory h 
        WHERE h.subscribe = :subscribe
          AND h.eventType = :eventType
          AND h.createdAt BETWEEN :start AND :end
    """)
    BigDecimal sumMonthlyDeposit(
            @Param("subscribe") Subscribe subscribe,
            @Param("eventType") SubscribeHistoryType eventType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}