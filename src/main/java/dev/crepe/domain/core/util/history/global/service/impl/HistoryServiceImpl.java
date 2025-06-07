package dev.crepe.domain.core.util.history.global.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import dev.crepe.domain.core.util.history.exchange.model.entity.ExchangeHistory;
import dev.crepe.domain.core.util.history.exchange.repositroy.ExchangeHistoryRepository;
import dev.crepe.domain.core.util.history.exchange.service.ExchangeHistoryService;
import dev.crepe.domain.core.util.history.global.service.HistoryService;
import dev.crepe.infra.redis.service.RedisHistoryService;
import dev.crepe.infra.redis.util.CachedSliceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class HistoryServiceImpl implements HistoryService {

    private final TransactionHistoryRepository txhRepo;
    private final ExchangeHistoryRepository exhRepo;
    private final AccountRepository accountRepository;
    private final TransactionHistoryService txhService;
    private final ExchangeHistoryService exhService;
    private final RedisHistoryService redisHistoryService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 캐시 TTL 설정
    private static final Duration ACCOUNT_CACHE_TTL = Duration.ofMinutes(10);
    private static final Duration TX_HISTORY_CACHE_TTL = Duration.ofMinutes(5);
    private static final Duration EXCHANGE_HISTORY_CACHE_TTL = Duration.ofMinutes(5);
    private static final Duration FINAL_RESULT_CACHE_TTL = Duration.ofMinutes(3);


    @Override
    public Slice<GetTransactionHistoryResponse> getNonRegulationHistory(String email, String currency, int page, int size) {
        String cacheKey = String.format("transactionHistory::%s:%s:%d:%d", email, currency, page, size);
        Slice<GetTransactionHistoryResponse> cachedResult = getCachedTransactionHistoryDirect(cacheKey);

        if (cachedResult != null && !cachedResult.getContent().isEmpty()) {
            log.debug("거래내역 캐시 히트 - email: {}, currency: {}, page: {}", email, currency, page);
            return cachedResult;
        }

        log.debug("거래내역 캐시 미스, DB 조회 시작 - email: {}, currency: {}", email, currency);

        // 2단계: 실제 DB 조회 수행
        Slice<GetTransactionHistoryResponse> result = performDatabaseQuery(email, currency, page, size);

        // 3단계: 조회 결과를 캐시에 저장
        cacheTransactionHistory(email, currency, page, size, result);

        // 4단계: 최근 거래내역도 별도 캐시 (실시간 조회용)
        if (page == 0 && !result.getContent().isEmpty()) {
            for (GetTransactionHistoryResponse transaction : result.getContent()) {
                redisHistoryService.cacheRecentTransaction(email, transaction);
            }
        }

        return result;
    }

    /**
     * 직접 Redis에서 캐시 조회 (RedisHistoryService의 @Cacheable 우회)
     */
    @SuppressWarnings("unchecked")
    private Slice<GetTransactionHistoryResponse> getCachedTransactionHistoryDirect(String cacheKey) {
        try {
            String cachedData = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                CachedSliceWrapper wrapper = objectMapper.readValue(cachedData, CachedSliceWrapper.class);
                log.debug("캐시에서 거래내역 조회 성공 - key: {}", cacheKey);
                return wrapper.toSlice();
            }
        } catch (Exception e) {
            log.warn("거래내역 캐시 조회 실패 - key: {}, error: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    /**
     * 실제 DB 조회 로직 분리
     */
    private Slice<GetTransactionHistoryResponse> performDatabaseQuery(String email, String currency, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        // 사용자 계좌 정보 캐시 조회
        List<Account> userAccounts = getCachedUserAccounts(email);
        if (userAccounts.isEmpty()) {
            return new SliceImpl<>(List.of(), pageRequest, false);
        }

        // 관련 계좌 필터링
        List<Account> relevantAccounts = userAccounts.stream()
                .filter(acc -> acc.getCoin() != null &&
                        acc.getCoin().getCurrency().equalsIgnoreCase(currency))
                .collect(Collectors.toList());

        if (relevantAccounts.isEmpty()) {
            return new SliceImpl<>(List.of(), pageRequest, false);
        }

        List<Long> relevantAccountIds = relevantAccounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        List<GetTransactionHistoryResponse> resultList = new ArrayList<>();

        // 일반 거래 이력 조회 (캐시 적용)
        for (Account acc : relevantAccounts) {
            if (acc.getCoin() != null) {
                List<GetTransactionHistoryResponse> txHistories = getCachedTransactionHistoriesByAccount(acc.getId());
                resultList.addAll(txHistories);
            }
        }

        // 환전 이력 조회 (캐시 적용)
        if (!relevantAccountIds.isEmpty()) {
            List<GetTransactionHistoryResponse> exchangeHistories =
                    getCachedExchangeHistoriesByAccounts(relevantAccountIds, relevantAccounts);
            resultList.addAll(exchangeHistories);
        }

        // 정렬 및 페이징
        resultList.sort(Comparator.comparing(GetTransactionHistoryResponse::getTransferredAt).reversed());

        int start = page * size;
        int end = Math.min(start + size, resultList.size());

        if (start >= resultList.size()) {
            return new SliceImpl<>(List.of(), pageRequest, false);
        }

        List<GetTransactionHistoryResponse> pageContent = resultList.subList(start, end);
        boolean hasNext = resultList.size() > end;

        return new SliceImpl<>(pageContent, pageRequest, hasNext);
    }

    // 사용자 계좌 캐시 조회 (기존과 동일)
    @SuppressWarnings("unchecked")
    private List<Account> getCachedUserAccounts(String email) {
        String cacheKey = "user_accounts:" + email;

        try {
            String cachedData = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return objectMapper.readValue(cachedData,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Account.class));
            }
        } catch (Exception e) {
            log.warn("사용자 계좌 캐시 조회 실패: {}", e.getMessage());
        }

        // 캐시 미스 시 DB에서 조회 후 캐시 저장
        List<Account> userAccounts = accountRepository.findByActor_Email(email);

        try {
            String jsonData = objectMapper.writeValueAsString(userAccounts);
            redisTemplate.opsForValue().set(cacheKey, jsonData, ACCOUNT_CACHE_TTL);
            log.debug("사용자 계좌 캐시 저장 완료 - email: {}, 계좌 수: {}", email, userAccounts.size());
        } catch (Exception e) {
            log.warn("사용자 계좌 캐시 저장 실패: {}", e.getMessage());
        }

        return userAccounts;
    }

    // 계좌별 거래내역 캐시 조회 (기존과 동일)
    @SuppressWarnings("unchecked")
    private List<GetTransactionHistoryResponse> getCachedTransactionHistoriesByAccount(Long accountId) {
        String cacheKey = "tx_histories_by_account:" + accountId;

        try {
            String cachedData = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return objectMapper.readValue(cachedData,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, GetTransactionHistoryResponse.class));
            }
        } catch (Exception e) {
            log.warn("계좌별 거래내역 캐시 조회 실패: {}", e.getMessage());
        }

        // 캐시 미스 시 DB에서 조회 후 캐시 저장
        List<TransactionHistory> txList = txhRepo.findByAccount_Id(accountId);
        List<GetTransactionHistoryResponse> responses = txList.stream()
                .map(txhService::getTransactionHistory)
                .collect(Collectors.toList());

        try {
            String jsonData = objectMapper.writeValueAsString(responses);
            redisTemplate.opsForValue().set(cacheKey, jsonData, TX_HISTORY_CACHE_TTL);
            log.debug("계좌별 거래내역 캐시 저장 완료 - accountId: {}, 내역 수: {}", accountId, responses.size());
        } catch (Exception e) {
            log.warn("계좌별 거래내역 캐시 저장 실패: {}", e.getMessage());
        }

        return responses;
    }

    // 환전내역 캐시 조회 (기존과 동일)
    @SuppressWarnings("unchecked")
    private List<GetTransactionHistoryResponse> getCachedExchangeHistoriesByAccounts(
            List<Long> relevantAccountIds, List<Account> relevantAccounts) {

        String cacheKey = "exchange_histories_by_accounts:" +
                relevantAccountIds.stream().map(String::valueOf).sorted().collect(Collectors.joining(","));

        try {
            String cachedData = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return objectMapper.readValue(cachedData,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, GetTransactionHistoryResponse.class));
            }
        } catch (Exception e) {
            log.warn("환전내역 캐시 조회 실패: {}", e.getMessage());
        }

        // 캐시 미스 시 DB에서 조회 후 캐시 저장
        List<ExchangeHistory> exList = exhRepo.findByFromAccount_IdInOrToAccount_IdIn(
                relevantAccountIds, relevantAccountIds);

        List<GetTransactionHistoryResponse> responses = exList.stream()
                .map(e -> {
                    Account matchedAccount = relevantAccounts.stream()
                            .filter(acc -> acc.getId().equals(e.getFromAccount().getId()) ||
                                    acc.getId().equals(e.getToAccount().getId()))
                            .findFirst()
                            .orElse(null);
                    return exhService.getExchangeHistory(e, matchedAccount);
                })
                .collect(Collectors.toList());

        try {
            String jsonData = objectMapper.writeValueAsString(responses);
            redisTemplate.opsForValue().set(cacheKey, jsonData, EXCHANGE_HISTORY_CACHE_TTL);
            log.debug("환전내역 캐시 저장 완료 - 계좌 수: {}, 내역 수: {}", relevantAccountIds.size(), responses.size());
        } catch (Exception e) {
            log.warn("환전내역 캐시 저장 실패: {}", e.getMessage());
        }

        return responses;
    }

    /**
     * 거래내역 결과 캐시 저장 - 확실히 저장되도록 개선
     */
    private void cacheTransactionHistory(String email, String currency, int page, int size,
                                         Slice<GetTransactionHistoryResponse> result) {
        try {
            String cacheKey = String.format("transactionHistory::%s:%s:%d:%d", email, currency, page, size);

            CachedSliceWrapper wrapper = CachedSliceWrapper.from(result);
            String jsonData = objectMapper.writeValueAsString(wrapper);

            // Redis에 저장
            redisTemplate.opsForValue().set(cacheKey, jsonData, FINAL_RESULT_CACHE_TTL);

            log.info("거래내역 결과 캐시 저장 완료 - email: {}, currency: {}, page: {}, size: {}, 결과 수: {}",
                    email, currency, page, size, result.getContent().size());

            // 추가로 RedisHistoryService의 캐시도 업데이트 (일관성 유지)
            try {
                redisHistoryService.updateTransactionHistoryCache(email, currency, page, size, result);
            } catch (Exception e) {
                log.warn("RedisHistoryService 캐시 업데이트 실패: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("거래내역 결과 캐시 저장 실패 - email: {}, currency: {}, error: {}",
                    email, currency, e.getMessage(), e);
        }
    }

    // 캐시 무효화 메서드들 (기존과 동일하지만 로그 개선)
    public void invalidateUserAccountsCache(String email) {
        String cacheKey = "user_accounts:" + email;
        Boolean deleted = redisTemplate.delete(cacheKey);
        log.info("사용자 계좌 캐시 무효화 - email: {}, 삭제 성공: {}", email, deleted);
    }

    public void invalidateTransactionHistoryByAccountCache(Long accountId) {
        String cacheKey = "tx_histories_by_account:" + accountId;
        Boolean deleted = redisTemplate.delete(cacheKey);
        log.info("계좌별 거래내역 캐시 무효화 - accountId: {}, 삭제 성공: {}", accountId, deleted);
    }

    public void invalidateUserTransactionHistoryCache(String email, String currency) {
        // 1. RedisHistoryService의 기존 메서드 활용
        try {
            redisHistoryService.evictUserHistoryCache(null); // userId는 사용하지 않으므로 null
        } catch (Exception e) {
            log.warn("RedisHistoryService 캐시 무효화 실패: {}", e.getMessage());
        }

        // 2. 직접 생성한 캐시도 무효화
        String pattern = String.format("transactionHistory::%s:%s:*", email, currency);
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            Long deletedCount = redisTemplate.delete(keys);
            log.info("사용자 거래내역 캐시 무효화 완료 - email: {}, currency: {}, 삭제된 키: {}/{}",
                    email, currency, deletedCount, keys.size());
        } else {
            log.debug("무효화할 거래내역 캐시가 없음 - email: {}, currency: {}", email, currency);
        }

        // 3. 사용자 계좌 캐시도 함께 무효화
        invalidateUserAccountsCache(email);
    }

    /**
     * 캐시 상태 확인용 메서드 (개발/디버깅용)
     */
    public void checkCacheStatus(String email, String currency, int page, int size) {
        String cacheKey = String.format("transactionHistory::%s:%s:%d:%d", email, currency, page, size);
        Boolean exists = redisTemplate.hasKey(cacheKey);

        if (exists) {
            try {
                String cachedData = (String) redisTemplate.opsForValue().get(cacheKey);
                CachedSliceWrapper wrapper = objectMapper.readValue(cachedData, CachedSliceWrapper.class);
                log.info("캐시 상태 확인 - key: {}, 존재: true, 내용 수: {}",
                        cacheKey, wrapper.getContent().size());
            } catch (Exception e) {
                log.warn("캐시 내용 확인 실패 - key: {}, error: {}", cacheKey, e.getMessage());
            }
        } else {
            log.info("캐시 상태 확인 - key: {}, 존재: false", cacheKey);
        }
    }

    /**
     * 특정 사용자의 모든 거래내역 캐시 무효화 (관리자용)
     */
    public void invalidateAllUserCaches(String email) {
        // 1. 사용자 계좌 캐시
        invalidateUserAccountsCache(email);

        // 2. 모든 화폐의 거래내역 캐시
        String pattern = "transactionHistory::" + email + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            Long deletedCount = redisTemplate.delete(keys);
            log.info("사용자 모든 거래내역 캐시 무효화 - email: {}, 삭제된 키: {}", email, deletedCount);
        }

        // 3. 최근 거래 캐시
        String recentKey = "recent:transaction:" + email;
        Boolean recentDeleted = redisTemplate.delete(recentKey);
        log.info("사용자 최근 거래 캐시 무효화 - email: {}, 삭제 성공: {}", email, recentDeleted);

        // 4. RedisHistoryService의 캐시도 정리
        try {
            redisHistoryService.evictUserHistoryCache(null);
        } catch (Exception e) {
            log.warn("RedisHistoryService 전체 캐시 무효화 실패: {}", e.getMessage());
        }
    }


}