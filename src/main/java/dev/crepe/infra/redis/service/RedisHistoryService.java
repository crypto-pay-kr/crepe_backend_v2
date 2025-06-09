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
     * 거래내역 캐시 업데이트
     */
    @CachePut(value = "transactionHistory",
            key = "#email + ':' + #currency + ':' + #page + ':' + #size")
    public Slice<GetTransactionHistoryResponse> updateTransactionHistoryCache(
            String email, String currency, int page, int size,
            Slice<GetTransactionHistoryResponse> data) {

        log.debug("거래내역 캐시 업데이트 - email: {}, currency: {}, page: {}, size: {}, 데이터 수: {}",
                email, currency, page, size, data.getContent().size());

        // 첫 페이지인 경우 최근 거래도 함께 캐시
        if (page == 0 && !data.getContent().isEmpty()) {
            GetTransactionHistoryResponse latestTransaction = data.getContent().get(0);
            cacheRecentTransaction(email, latestTransaction);
            log.debug("최근 거래 캐시도 함께 업데이트 - email: {}", email);
        }

        return data;
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
     * 최근 거래 캐시 갱신
     */
    public void refreshRecentTransactionCache(String userEmail) {
        try {
            String recentKey = "recent:transaction:" + userEmail;

            // 기존 캐시 삭제
            Boolean deleted = redisTemplate.delete(recentKey);
            log.debug("최근 거래 캐시 삭제 - email: {}, 삭제 성공: {}", userEmail, deleted);

            // 새로운 최근 거래는 다음 거래 발생 시 자동으로 추가됨

        } catch (Exception e) {
            log.error("최근 거래 캐시 갱신 실패 - email: {}, error: {}", userEmail, e.getMessage(), e);
        }
    }

    /**
     * 특정 계좌 관련 캐시 삭제
     */
    public void evictAccountRelatedCache(Long accountId, String userEmail) {
        try {
            // 계좌별 거래내역 캐시 삭제
            String accountCacheKey = "tx_histories_by_account:" + accountId;
            Boolean accountDeleted = redisTemplate.delete(accountCacheKey);
            log.debug("계좌별 거래내역 캐시 삭제 - accountId: {}, 삭제 성공: {}", accountId, accountDeleted);

            // 사용자 계좌 정보 캐시 삭제
            if (userEmail != null) {
                String userAccountKey = "user_accounts:" + userEmail;
                Boolean userAccountDeleted = redisTemplate.delete(userAccountKey);
                log.debug("사용자 계좌 캐시 삭제 - email: {}, 삭제 성공: {}", userEmail, userAccountDeleted);
            }

            // 환전내역 캐시는 계좌 ID 조합이므로 패턴 삭제
            String exchangePattern = "exchange_histories_by_accounts:*" + accountId + "*";
            Set<String> exchangeKeys = redisTemplate.keys(exchangePattern);
            if (exchangeKeys != null && !exchangeKeys.isEmpty()) {
                Long exchangeDeleted = redisTemplate.delete(exchangeKeys);
                log.debug("환전내역 캐시 삭제 - accountId: {}, 삭제된 키: {}", accountId, exchangeDeleted);
            }

            log.info("계좌 관련 캐시 삭제 완료 - accountId: {}, userEmail: {}", accountId, userEmail);

        } catch (Exception e) {
            log.error("계좌 관련 캐시 삭제 실패 - accountId: {}, userEmail: {}, error: {}",
                    accountId, userEmail, e.getMessage(), e);
        }
    }


    /**
     * 특정 사용자의 거래내역 캐시 삭제
     */
    @CacheEvict(value = {"transactionHistory", "exchangeHistory"}, allEntries = true)
    public void evictUserTransactionHistoryCache(String email) {
        evictUserTransactionHistoryCacheAndRefresh(email);
    }

    /**
     * 사용자 거래내역 캐시 무효화 + 기본 페이지 재생성
     */
    @CacheEvict(value = {"transactionHistory", "exchangeHistory"}, allEntries = true)
    public void evictUserTransactionHistoryCacheAndRefresh(String email) {
        try {
            log.info("사용자 거래내역 캐시 무효화 시작 - email: {}", email);

            // 1. 기존 사용자 거래내역 캐시 모두 삭제
            String[] patterns = {
                    TRANSACTION_HISTORY_PREFIX + email + "*",
                    EXCHANGE_HISTORY_PREFIX + email + "*",
                    "transactionHistory::" + email + ":*"
            };

            int totalDeleted = 0;
            for (String pattern : patterns) {
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    Long deletedCount = redisTemplate.delete(keys);
                    totalDeleted += deletedCount != null ? deletedCount.intValue() : 0;
                    log.debug("패턴 {} 매칭 캐시 {} 개 삭제", pattern, deletedCount);
                }
            }

            log.info("사용자 거래내역 캐시 무효화 완료 - email: {}, 총 삭제된 키: {}", email, totalDeleted);

            // 2. 최근 거래 캐시 갱신
            refreshRecentTransactionCache(email);

        } catch (Exception e) {
            log.error("사용자 거래내역 캐시 갱신 실패 - email: {}, error: {}", email, e.getMessage(), e);
        }
    }

    /**
     * 모든 히스토리 캐시 초기화
     */
    @CacheEvict(value = {"payHistory", "settlementHistory", "transactionHistory", "exchangeHistory"},
            allEntries = true)
    public void evictAllHistoryCache() {
        try {
            log.info("모든 히스토리 캐시 초기화 시작");

            // Redis에서 히스토리 관련 모든 키 삭제
            String[] patterns = {
                    "history:*",
                    "stats:*",
                    "recent:*",
                    "transactionHistory::*",
                    "tx_histories_by_account:*",
                    "exchange_histories_by_accounts:*",
                    "user_accounts:*"
            };

            int totalDeleted = 0;
            for (String pattern : patterns) {
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    Long deletedCount = redisTemplate.delete(keys);
                    totalDeleted += deletedCount != null ? deletedCount.intValue() : 0;
                    log.info("패턴 {} 매칭 캐시 {} 개 삭제", pattern, deletedCount);
                }
            }

            log.info("모든 히스토리 캐시 초기화 완료 - 총 삭제된 키: {}", totalDeleted);

        } catch (Exception e) {
            log.error("모든 히스토리 캐시 초기화 실패", e);
        }
    }

    /**
     * 히스토리 통계 캐시 저장
     */
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