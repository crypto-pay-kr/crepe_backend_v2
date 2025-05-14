package dev.crepe.domain.core.util.history.subscribe.repository;

import dev.crepe.domain.core.util.history.subscribe.model.entity.SubscribeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscribeHistoryRepsitory extends JpaRepository<SubscribeHistory,Long> {
}
