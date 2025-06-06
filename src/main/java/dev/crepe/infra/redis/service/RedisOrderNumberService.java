// RedisOrderNumberService.java
package dev.crepe.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisOrderNumberService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String ORDER_NUMBER_KEY_PREFIX = "orderNumber:";

    public synchronized String generateOrderNumber(Long storeId, String orderId) {
        String currentKey = ORDER_NUMBER_KEY_PREFIX + storeId + ":current";
        Long currentNumber = redisTemplate.opsForValue().increment(currentKey, 1);
        String clientOrderNumber = currentNumber.toString();
        String mappingKey = ORDER_NUMBER_KEY_PREFIX + storeId + ":" + orderId;
        redisTemplate.opsForValue().set(mappingKey, clientOrderNumber);
        return clientOrderNumber;
    }

    public String getOrderNumber(Long storeId, String orderId) {
        String mappingKey = ORDER_NUMBER_KEY_PREFIX + storeId + ":" + orderId;
        return redisTemplate.opsForValue().get(mappingKey);
    }

    public void resetAllOrderNumbers(List<Long> storeIds) {
        storeIds.forEach(storeId -> {
            String currentKey = ORDER_NUMBER_KEY_PREFIX + storeId + ":current";
            redisTemplate.delete(currentKey);
        });
    }
}