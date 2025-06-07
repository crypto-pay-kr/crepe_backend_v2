package dev.crepe.infra.redis.util;

import dev.crepe.domain.channel.actor.service.ActorHistoryService;
import dev.crepe.infra.redis.service.RedisHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCacheInvalidationListener {

    private final RedisHistoryService redisHistoryService;
    private final ActorHistoryService actorHistoryService;

    @EventListener
    @Async("cacheTaskExecutor") // 기존 설정의 비동기 Executor 사용
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        try {
            // 계좌 관련 캐시 무효화
            redisHistoryService.evictAccountRelatedCache(event.getAccountId(), event.getUserEmail());

            // 사용자별 거래내역 캐시 무효화
            if (event.getUserEmail() != null) {
                redisHistoryService.evictUserTransactionHistoryCache(event.getUserEmail());

                // 최근 거래 캐시에 새 거래 추가
                if (event.getTransactionResponse() != null) {
                    redisHistoryService.cacheRecentTransaction(event.getUserEmail(), event.getTransactionResponse());
                }
            }

            log.info("거래내역 캐시 무효화 완료 - AccountId: {}, Email: {}, Type: {}",
                    event.getAccountId(), event.getUserEmail(), event.getTransactionType());

        } catch (Exception e) {
            log.error("거래내역 캐시 무효화 실패", e);
        }
    }
}
