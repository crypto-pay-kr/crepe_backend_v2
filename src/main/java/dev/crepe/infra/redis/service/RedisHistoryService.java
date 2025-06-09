package dev.crepe.infra.redis.service;

import dev.crepe.domain.admin.dto.response.GetPayHistoryResponse;
import dev.crepe.domain.admin.dto.response.GetSettlementHistoryResponse;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisHistoryService {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PAY_HISTORY_PREFIX = "history:pay:";
    private static final String SETTLEMENT_HISTORY_PREFIX = "history:settlement:";
    private static final String TRANSACTION_HISTORY_PREFIX = "history:transaction:";
    private static final String EXCHANGE_HISTORY_PREFIX = "history:exchange:";

    /**
     * 결제 내역 캐시 조회
     */
    @Cacheable(value = "settlementHistory", key="#userId + ':' + #type + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<GetPayHistoryResponse> getPayHistoriesByUserId(Long userId, String type, Pageable pageable) {
        log.debug("결제 내역 캐시 조회 - userId: {}, type: {}, page: {}", userId, type, pageable.getPageNumber());
        return null;
    }

    /**
     * 정산 내역 캐시 조회
     */
    @Cacheable(value = "settlementHistory",
            key = "#storeId + ':' + (#status != null ? #status.name() : 'ALL') + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<GetSettlementHistoryResponse> getSettlementHistoriesByStoreId(Long storeId, Object status, Pageable pageable) {
        log.debug("정산 내역 캐시 조회 - storeId: {}, status: {}, page: {}", storeId, status, pageable.getPageNumber());
        return null;
    }

    /**
     * 환전 내역 캐시 조회
     */
    @Cacheable(value = "exchangeHistory",
            key = "#email + ':' + #currency + ':' + #page + ':' + #size")
    public Slice<GetTransactionHistoryResponse> getExchangeHistory(String email, String currency, int page, int size) {
        log.debug("환전 내역 캐시 조회 - email: {}, currency: {}, page: {}", email, currency, page);
        return null;
    }

    /**
     * 사용자별 히스토리 캐시 갱신
     */
    @CachePut(value = "payHistory", key = "#userId + ':' + #type + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<GetPayHistoryResponse> updatePayHistoryCache(Long userId, String type, Pageable pageable, Page<GetPayHistoryResponse> data) {
        log.debug("결제 내역 캐시 갱신 - userId: {}, type: {}", userId, type);
        return data;
    }

    /**
     * 사용자별 히스토리 캐시 삭제 (히스토리 데이터만)
     */
    @CacheEvict(value = {"payHistory", "settlementHistory", "transactionHistory", "exchangeHistory"},
            allEntries = true)
    public void evictUserHistoryCache(Long userId) {
        log.info("사용자 히스토리 캐시 삭제 - userId: {}", userId);

        // 히스토리 관련 캐시만 패턴 매칭으로 삭제
        String[] patterns = {
                PAY_HISTORY_PREFIX + userId + "*",
                TRANSACTION_HISTORY_PREFIX + "*:" + userId + "*",
                EXCHANGE_HISTORY_PREFIX + "*:" + userId + "*"
        };

        for (String pattern : patterns) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("패턴 {} 매칭 히스토리 캐시 {} 개 삭제", pattern, keys.size());
            }
        }
    }


    public void cacheHistoryStats(String key, Object stats, Duration ttl) {
        redisTemplate.opsForValue().set("stats:" + key, stats, ttl);
        log.debug("히스토리 통계 캐시 저장 - key: {}, ttl: {}", key, ttl);
    }

    /**
     * 히스토리 통계 정보 조회
     */
    @SuppressWarnings("unchecked")
    public <T> T getHistoryStats(String key, Class<T> type) {
        Object result = redisTemplate.opsForValue().get("stats:" + key);
        if (type.isInstance(result)) {
            log.debug("히스토리 통계 캐시 조회 성공 - key: {}", key);
            return type.cast(result);
        }
        log.debug("히스토리 통계 캐시 없음 - key: {}", key);
        return null;
    }

    /**
     * 최근 거래 내역 캐시 (실시간용)
     */
    public void cacheRecentTransaction(String userEmail, GetTransactionHistoryResponse transaction) {
        String key = "recent:transaction:" + userEmail;

        // 리스트의 앞쪽에 추가 (최신 순)
        redisTemplate.opsForList().leftPush(key, transaction);

        // 최대 50개까지만 유지
        redisTemplate.opsForList().trim(key, 0, 49);

        // TTL 설정 (1시간)
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        log.debug("최근 거래 내역 캐시 추가 - userEmail: {}", userEmail);
    }

    /**
     * 최근 거래 내역 조회
     */
    @SuppressWarnings("unchecked")
    public List<GetTransactionHistoryResponse> getRecentTransactions(String userEmail, long count) {
        String key = "recent:transaction:" + userEmail;
        List<Object> result = redisTemplate.opsForList().range(key, 0, count - 1);

        if (result != null && !result.isEmpty()) {
            log.debug("최근 거래 내역 캐시 조회 - userEmail: {}, count: {}", userEmail, result.size());
            return result.stream()
                    .filter(GetTransactionHistoryResponse.class::isInstance)
                    .map(GetTransactionHistoryResponse.class::cast)
                    .toList();
        }

        return List.of();
    }


    /**
     * 거래 내역 캐시 저장
     */
    @CachePut(value = "transactionHistory",
            key = "#email + ':' + #currency + ':' + #page + ':' + #size")
    public Slice<GetTransactionHistoryResponse> updateTransactionHistoryCache(
            String email, String currency, int page, int size,
            Slice<GetTransactionHistoryResponse> data) {
        log.debug("거래 내역 캐시 갱신 - email: {}, currency: {}, page: {}", email, currency, page);
        return data;
    }

    /**
     * 특정 사용자의 거래내역 캐시 삭제
     */
    @CacheEvict(value = {"transactionHistory", "exchangeHistory"}, allEntries = true)
    public void evictUserTransactionHistoryCache(String email) {
        log.info("사용자 거래내역 캐시 삭제 - email: {}", email);

        // 패턴 매칭으로 관련 캐시 삭제
        String[] patterns = {
                TRANSACTION_HISTORY_PREFIX + email + "*",
                EXCHANGE_HISTORY_PREFIX + email + "*",
                "tx_histories_by_account:*",
                "exchange_histories_by_accounts:*",
                "user_accounts:" + email
        };

        for (String pattern : patterns) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("패턴 {} 매칭 거래내역 캐시 {} 개 삭제", pattern, keys.size());
            }
        }
    }

    /**
     * 특정 계좌 관련 캐시 삭제
     */
    public void evictAccountRelatedCache(Long accountId, String userEmail) {
        // 계좌별 거래내역 캐시 삭제
        String accountCacheKey = "tx_histories_by_account:" + accountId;
        redisTemplate.delete(accountCacheKey);

        // 사용자 계좌 정보 캐시 삭제
        if (userEmail != null) {
            String userAccountKey = "user_accounts:" + userEmail;
            redisTemplate.delete(userAccountKey);
        }

        // 환전내역 캐시는 계좌 ID 조합이므로 패턴 삭제
        String exchangePattern = "exchange_histories_by_accounts:*" + accountId + "*";
        Set<String> exchangeKeys = redisTemplate.keys(exchangePattern);
        if (exchangeKeys != null && !exchangeKeys.isEmpty()) {
            redisTemplate.delete(exchangeKeys);
        }

        log.info("계좌 관련 캐시 삭제 완료 - accountId: {}, userEmail: {}", accountId, userEmail);
    }

    /**
     * 모든 히스토리 캐시 초기화 (관리자용)
     */
    @CacheEvict(value = {"payHistory", "settlementHistory", "transactionHistory", "exchangeHistory"},
            allEntries = true)
    public void evictAllHistoryCache() {
        log.info("모든 히스토리 캐시 초기화");

        // Redis에서 히스토리 관련 모든 키 삭제
        String[] patterns = {
                "history:*",
                "stats:*",
                "recent:*"
        };

        for (String pattern : patterns) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("패턴 {} 매칭 캐시 {} 개 삭제", pattern, keys.size());
            }
        }
    }

    /**
     * 캐시 상태 확인
     */
    public boolean isCacheAvailable() {
        try {
            redisTemplate.opsForValue().set("health:check", "ok", Duration.ofSeconds(10));
            String result = (String) redisTemplate.opsForValue().get("health:check");
            return "ok".equals(result);
        } catch (Exception e) {
            log.error("Redis 캐시 상태 확인 실패", e);
            return false;
        }
    }

}
