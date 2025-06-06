package dev.crepe.domain.channel.market.order.scheduler;

import dev.crepe.domain.channel.actor.store.repository.StoreRepository;
import dev.crepe.infra.redis.service.RedisOrderNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderNumberScheduler {
    private final RedisOrderNumberService redisOrderNumberService;
    private final StoreRepository storeRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void resetOrderNumbers() {
        List<Long> storeIds = storeRepository.findAll().stream()
                .map(store -> store.getId())
                .toList();
        redisOrderNumberService.resetAllOrderNumbers(storeIds);
    }
}