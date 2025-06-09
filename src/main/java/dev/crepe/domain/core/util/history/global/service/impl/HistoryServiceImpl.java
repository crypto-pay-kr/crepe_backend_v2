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

    // 사용자 계좌 캐시 조회
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

    // 계좌별 거래내역 캐시 조회
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

    // 환전내역 캐시 조회
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
     * 거래내역 결과 캐시 저장
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
     * 사용자 계좌 캐시 무효화 + 즉시 재생성
     */
    public void invalidateUserAccountsCache(String email) {
        try {
            String cacheKey = "user_accounts:" + email;
            Boolean deleted = redisTemplate.delete(cacheKey);
            log.info("사용자 계좌 캐시 삭제 - email: {}, 삭제 성공: {}", email, deleted);

            // 즉시 최신 데이터로 재생성
            List<Account> freshData = getCachedUserAccounts(email);
            log.info("사용자 계좌 캐시 재생성 완료 - email: {}, 계좌 수: {}", email, freshData.size());

        } catch (Exception e) {
            log.error("사용자 계좌 캐시 갱신 실패 - email: {}, error: {}", email, e.getMessage(), e);
        }
    }

    /**
     * 계좌별 거래내역 캐시 무효화 + 즉시 재생성
     */
    public void invalidateTransactionHistoryByAccountCache(Long accountId) {
        try {
            String cacheKey = "tx_histories_by_account:" + accountId;
            Boolean deleted = redisTemplate.delete(cacheKey);
            log.info("계좌별 거래내역 캐시 삭제 - accountId: {}, 삭제 성공: {}", accountId, deleted);

            // 즉시 최신 데이터로 재생성
            List<GetTransactionHistoryResponse> freshData = getCachedTransactionHistoriesByAccount(accountId);
            log.info("계좌별 거래내역 캐시 재생성 완료 - accountId: {}, 내역 수: {}", accountId, freshData.size());

        } catch (Exception e) {
            log.error("계좌별 거래내역 캐시 갱신 실패 - accountId: {}, error: {}", accountId, e.getMessage(), e);
        }
    }

    /**
     * 사용자 거래내역 캐시 무효화 + 첫 페이지 재생성
     */
    public void invalidateUserTransactionHistoryCache(String email, String currency) {
        try {
            // 1. RedisHistoryService의 사용자 거래내역 캐시 삭제
            try {
                redisHistoryService.evictUserTransactionHistoryCache(email);
            } catch (Exception e) {
                log.warn("RedisHistoryService 캐시 무효화 실패: {}", e.getMessage());
            }

            // 2. 특정 통화의 모든 페이지 캐시 삭제 (수정된 패턴)
            String specificPattern = String.format("transactionHistory::%s:%s:*", email, currency);
            Set<String> specificKeys = redisTemplate.keys(specificPattern);
            if (specificKeys != null && !specificKeys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(specificKeys);
                log.info("특정 통화 거래내역 캐시 삭제 완료 - email: {}, currency: {}, 삭제된 키: {}/{}",
                        email, currency, deletedCount, specificKeys.size());
            }

            // 3. 사용자의 모든 거래내역 캐시도 삭제 (전체 패턴)
            String allPattern = String.format("transactionHistory::%s:*", email);
            Set<String> allKeys = redisTemplate.keys(allPattern);
            if (allKeys != null && !allKeys.isEmpty()) {
                Long allDeletedCount = redisTemplate.delete(allKeys);
                log.info("사용자 모든 거래내역 캐시 삭제 완료 - email: {}, 삭제된 키: {}", email, allDeletedCount);
            }

            // 4. 사용자 계좌 캐시도 갱신
            invalidateUserAccountsCache(email);

            // 5. 첫 번째 페이지 즉시 재생성 (가장 많이 조회되는 페이지)
            int defaultPage = 0;
            int defaultSize = 20;

            log.info("사용자 거래내역 첫 페이지 재생성 시작 - email: {}, currency: {}", email, currency);
            Slice<GetTransactionHistoryResponse> firstPageData = performDatabaseQuery(email, currency, defaultPage, defaultSize);
            cacheTransactionHistory(email, currency, defaultPage, defaultSize, firstPageData);

            log.info("사용자 거래내역 캐시 갱신 완료 - email: {}, currency: {}, 첫 페이지 데이터 수: {}",
                    email, currency, firstPageData.getContent().size());

        } catch (Exception e) {
            log.error("사용자 거래내역 캐시 갱신 실패 - email: {}, currency: {}, error: {}",
                    email, currency, e.getMessage(), e);
        }
    }



    public void forceEvictUserCurrencyCache(String email, String currency) {
        try {
            log.info("특정 통화 캐시 강제 삭제 시작 - email: {}, currency: {}", email, currency);

            // 1. 해당 통화의 모든 페이지 캐시 삭제
            String pattern = String.format("transactionHistory::%s:%s:*", email, currency);
            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                log.info("삭제할 캐시 키들: {}", keys);
                Long deletedCount = redisTemplate.delete(keys);
                log.info("특정 통화 캐시 강제 삭제 완료 - email: {}, currency: {}, 삭제된 키: {}",
                        email, currency, deletedCount);
            } else {
                log.info("삭제할 캐시가 없음 - email: {}, currency: {}", email, currency);
            }

            // 2. 최근 거래 캐시도 삭제
            String recentKey = "recent:transaction:" + email;
            Boolean recentDeleted = redisTemplate.delete(recentKey);
            log.info("최근 거래 캐시 삭제 - email: {}, 삭제 성공: {}", email, recentDeleted);

        } catch (Exception e) {
            log.error("특정 통화 캐시 강제 삭제 실패 - email: {}, currency: {}, error: {}",
                    email, currency, e.getMessage(), e);
        }
    }




    public Slice<GetTransactionHistoryResponse> getNonRegulationHistoryPureDB(String email, String currency, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        // 사용자 계좌 정보 직접 DB 조회 (캐시 없이)
        List<Account> userAccounts = accountRepository.findByActor_Email(email);
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

        // 일반 거래 이력 직접 DB 조회 (캐시 없이)
        for (Account acc : relevantAccounts) {
            if (acc.getCoin() != null) {
                List<TransactionHistory> txList = txhRepo.findByAccount_Id(acc.getId());
                List<GetTransactionHistoryResponse> responses = txList.stream()
                        .map(txhService::getTransactionHistory)
                        .collect(Collectors.toList());
                resultList.addAll(responses);
            }
        }

        // 환전 이력 직접 DB 조회 (캐시 없이)
        if (!relevantAccountIds.isEmpty()) {
            List<ExchangeHistory> exList = exhRepo.findByFromAccount_IdInOrToAccount_IdIn(
                    relevantAccountIds, relevantAccountIds);

            List<GetTransactionHistoryResponse> exchangeResponses = exList.stream()
                    .map(e -> {
                        Account matchedAccount = relevantAccounts.stream()
                                .filter(acc -> acc.getId().equals(e.getFromAccount().getId()) ||
                                        acc.getId().equals(e.getToAccount().getId()))
                                .findFirst()
                                .orElse(null);
                        return exhService.getExchangeHistory(e, matchedAccount);
                    })
                    .collect(Collectors.toList());
            resultList.addAll(exchangeResponses);
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

    @Override
    public Slice<GetTransactionHistoryResponse> getNonRegulationHistoryOptimizedCache(String email, String currency, int page, int size) {
        // 최종 결과 캐시만 확인 (다른 중간 캐시는 건너뛰기)
        String cacheKey = String.format("transactionHistory::%s:%s:%d:%d", email, currency, page, size);

        try {
            String cachedData = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                CachedSliceWrapper wrapper = objectMapper.readValue(cachedData, CachedSliceWrapper.class);
                log.debug("최적화 캐시 히트 - key: {}", cacheKey);
                return wrapper.toSlice();
            }
        } catch (Exception e) {
            log.warn("최적화 캐시 조회 실패 - key: {}, error: {}", cacheKey, e.getMessage());
        }

        // 캐시 미스 시 순수 DB 조회 후 캐시 저장
        Slice<GetTransactionHistoryResponse> result = getNonRegulationHistoryPureDB(email, currency, page, size);

        // 결과만 캐시에 저장 (중간 단계 캐시는 생략)
        try {
            CachedSliceWrapper wrapper = CachedSliceWrapper.from(result);
            String jsonData = objectMapper.writeValueAsString(wrapper);
            redisTemplate.opsForValue().set(cacheKey, jsonData, FINAL_RESULT_CACHE_TTL);
            log.debug("최적화 캐시 저장 완료 - key: {}", cacheKey);
        } catch (Exception e) {
            log.warn("최적화 캐시 저장 실패 - key: {}, error: {}", cacheKey, e.getMessage());
        }

        return result;
    }
}