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
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Qualifier("redisObjectMapper")
    private final ObjectMapper objectMapper;

    // 캐시 TTL 설정
    private static final Duration ACCOUNT_CACHE_TTL = Duration.ofMinutes(10);
    private static final Duration TX_HISTORY_CACHE_TTL = Duration.ofMinutes(5);
    private static final Duration EXCHANGE_HISTORY_CACHE_TTL = Duration.ofMinutes(5);
    private static final Duration FINAL_RESULT_CACHE_TTL = Duration.ofMinutes(3);

    private String sanitizeKey(String email) {    return email.replace("@", "_at_")
            .replace(".", "_dot_");}

    @Override
    public Slice<GetTransactionHistoryResponse> getNonRegulationHistory(String email, String currency, int page, int size) {
        String cacheKey = String.format("transactionHistory::%s:%s:%d:%d", sanitizeKey(email), currency, page, size);

        // 1단계: 캐시 조회
        Slice<GetTransactionHistoryResponse> cachedResult = getCachedTransactionHistoryDirect(cacheKey);
        if (cachedResult != null) {
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
     * Redis에서 직접 캐시 조회 - RedisTemplate의 직렬화기 사용
     */
    private Slice<GetTransactionHistoryResponse> getCachedTransactionHistoryDirect(String cacheKey) {
        log.debug("캐시 조회 시도 - key: {}", cacheKey);

        try {
            // RedisTemplate의 ValueSerializer를 사용해서 역직렬화
            Object cachedObject = redisTemplate.opsForValue().get(cacheKey);
            log.debug("캐시 데이터 존재 여부 - key: {}, 존재: {}", cacheKey, cachedObject != null);

            if (cachedObject != null) {
                // GenericJackson2JsonRedisSerializer가 이미 역직렬화한 객체를 받음
                if (cachedObject instanceof CachedSliceWrapper) {
                    log.debug("캐시에서 거래내역 조회 성공 (CachedSliceWrapper) - key: {}", cacheKey);
                    return ((CachedSliceWrapper) cachedObject).toSlice();
                }

                // 만약 Map이나 다른 형태로 역직렬화되었다면 ObjectMapper로 변환
                CachedSliceWrapper wrapper = objectMapper.convertValue(cachedObject, CachedSliceWrapper.class);
                log.debug("캐시에서 거래내역 조회 성공 (convertValue) - key: {}", cacheKey);
                return wrapper.toSlice();
            }
        } catch (Exception e) {
            log.warn("거래내역 캐시 조회 실패 - key: {}, error: {}", cacheKey, e.getMessage(), e);
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


    @SuppressWarnings("unchecked")
    private List<Account> getCachedUserAccounts(String email) {
        String cacheKey = "user_accounts:" + email;

        try {
            Object cachedObject = redisTemplate.opsForValue().get(cacheKey);
            if (cachedObject != null) {
                // GenericJackson2JsonRedisSerializer가 List로 역직렬화했는지 확인
                if (cachedObject instanceof List) {
                    return (List<Account>) cachedObject;
                }
                // 다른 형태로 역직렬화되었다면 변환
                return objectMapper.convertValue(cachedObject,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Account.class));
            }
        } catch (Exception e) {
            log.warn("사용자 계좌 캐시 조회 실패: {}", e.getMessage());
        }

        // 캐시 미스 시 DB에서 조회 후 캐시 저장
        List<Account> userAccounts = accountRepository.findByActor_Email(email);

        try {
            redisTemplate.opsForValue().set(cacheKey, userAccounts, ACCOUNT_CACHE_TTL);
            log.debug("사용자 계좌 캐시 저장 완료 - email: {}, 계좌 수: {}", email, userAccounts.size());
        } catch (Exception e) {
            log.warn("사용자 계좌 캐시 저장 실패: {}", e.getMessage());
        }

        return userAccounts;
    }

    @SuppressWarnings("unchecked")
    private List<GetTransactionHistoryResponse> getCachedTransactionHistoriesByAccount(Long accountId) {
        String cacheKey = "tx_histories_by_account:" + accountId;

        try {
            Object cachedObject = redisTemplate.opsForValue().get(cacheKey);
            if (cachedObject != null) {
                if (cachedObject instanceof List) {
                    return (List<GetTransactionHistoryResponse>) cachedObject;
                }
                return objectMapper.convertValue(cachedObject,
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
            redisTemplate.opsForValue().set(cacheKey, responses, TX_HISTORY_CACHE_TTL);
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
            Object cachedObject = redisTemplate.opsForValue().get(cacheKey);
            if (cachedObject != null) {
                if (cachedObject instanceof List) {
                    return (List<GetTransactionHistoryResponse>) cachedObject;
                }
                return objectMapper.convertValue(cachedObject,
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
            redisTemplate.opsForValue().set(cacheKey, responses, EXCHANGE_HISTORY_CACHE_TTL);
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
            String cacheKey = String.format("transactionHistory::%s:%s:%d:%d", sanitizeKey(email), currency, page, size);

            CachedSliceWrapper wrapper = CachedSliceWrapper.from(result);

            // RedisTemplate의 ValueSerializer를 사용해서 직렬화
            redisTemplate.opsForValue().set(cacheKey, wrapper, FINAL_RESULT_CACHE_TTL);

            log.info("거래내역 결과 캐시 저장 완료 - email: {}, currency: {}, page: {}, size: {}, 결과 수: {}",
                    email, currency, page, size, result.getContent().size());

        } catch (Exception e) {
            log.error("거래내역 결과 캐시 저장 실패 - email: {}, currency: {}, error: {}",
                    email, currency, e.getMessage(), e);
        }
    }

}