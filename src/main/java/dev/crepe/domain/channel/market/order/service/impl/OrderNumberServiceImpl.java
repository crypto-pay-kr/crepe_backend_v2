package dev.crepe.domain.channel.market.order.service.impl;

import dev.crepe.domain.channel.actor.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderNumberServiceImpl {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_ORDER_NUMBER = 200;
    private final StoreRepository storeRepository;

    public String getNextOrderNumber(Long storeId) {
        String storeOrderKey = "order_numbers:" + storeId; // Store별 키 생성
        Long nextOrderNumber = redisTemplate.opsForValue().increment(storeOrderKey);
        if (nextOrderNumber > MAX_ORDER_NUMBER) {
            throw new IllegalStateException("Order number limit exceeded for store ID: " + storeId);
        }
        return String.valueOf(nextOrderNumber);
    }

    public void resetOrderNumbers(Long storeId) {
        String storeOrderKey = "order_numbers:" + storeId; // Store별 키 생성
        redisTemplate.delete(storeOrderKey);
        redisTemplate.opsForValue().set(storeOrderKey, "0", 1, TimeUnit.DAYS);
    }

    public void resetOrderNumbersForAllStores() {
        List<Long> storeIds = storeRepository.findAllStoreIds(); // 모든 Store ID 조회
        for (Long storeId : storeIds) {
            resetOrderNumbers(storeId);
        }
    }
}