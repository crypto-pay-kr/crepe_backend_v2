package dev.crepe.domain.core.util.history.subscribe.repository;

import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.util.history.subscribe.model.SubscribeHistoryType;
import dev.crepe.domain.core.util.history.subscribe.model.dto.SubscribeHistoryDto;
import dev.crepe.domain.core.util.history.subscribe.model.entity.SubscribeHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Query("""
    SELECT COUNT(sh)
    FROM SubscribeHistory sh
    WHERE sh.subscribe = :subscribe
      AND sh.eventType = :eventType
      AND sh.createdAt BETWEEN :start AND :end
""")
    int countMonthlyDepositEvents(
            @Param("subscribe") Subscribe subscribe,
            @Param("eventType") SubscribeHistoryType eventType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    Slice<SubscribeHistory> findAllBySubscribe_IdOrderByCreatedAtDesc(Long subscribeId, Pageable pageable);

    List<SubscribeHistory> findBySubscribe_User_Email(String email);
}