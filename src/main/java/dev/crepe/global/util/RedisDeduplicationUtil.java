package dev.crepe.global.util;

import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisDeduplicationUtil {

    private final RedisTemplate<String, String> redisTemplate;
    private final ExceptionDbService exceptionDbService;

    public void checkAndStoreIfDuplicate(String key) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key,"1", Duration.ofMinutes(10));
        log.info("Redis Deduplication Check: key={}, success={}", key, success);
        if (Boolean.FALSE.equals(success)) {
            throw exceptionDbService.getException("DUPLICATE_REQUEST_001");
        }
    }
}