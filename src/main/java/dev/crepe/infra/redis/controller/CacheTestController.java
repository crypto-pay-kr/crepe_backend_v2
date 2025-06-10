package dev.crepe.infra.redis.controller;

import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.global.service.impl.HistoryServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/history/test")
@RequiredArgsConstructor
@Slf4j
public class CacheTestController {

    private final HistoryServiceImpl historyService;

    @PostMapping("/compare-all")
    public ResponseEntity<Map<String, Object>> compareAllMethods(
            @RequestParam String currency,
            @RequestParam(defaultValue = "100") int iterations, // 기본값 100으로 변경
            AppAuthentication auth) {

        String email = auth.getUserEmail();

        // 📊 먼저 데이터 개수 확인
        Slice<GetTransactionHistoryResponse> sampleData = historyService.getNonRegulationHistoryPureDB(email, currency, 0, 100);
        int dataCount = sampleData.getContent().size();
        boolean hasMoreData = sampleData.hasNext();

        log.info("🎯 성능 비교 시작 - email: {}, currency: {}, 반복: {}회", email, currency, iterations);
        log.info("📊 현재 {}에 {}건의 거래 데이터 존재 (더 있음: {})", currency, dataCount, hasMoreData);

        Map<String, Object> results = new HashMap<>();

        // 1. 순수 DB 조회 테스트
        log.info("⏱️ 순수 DB 조회 테스트 시작...");
        long pureDBTotal = 0;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            historyService.getNonRegulationHistoryPureDB(email, currency, 0, 20);
            long endTime = System.currentTimeMillis();
            pureDBTotal += (endTime - startTime);

            // 진행률 표시 (25% 단위)
            if ((i + 1) % (iterations / 4) == 0) {
                log.info("순수 DB 테스트 진행률: {}% ({}/{})",
                        (i + 1) * 100 / iterations, i + 1, iterations);
            }
        }

        // 2. 최적화된 캐시 방식 테스트 (캐시 삭제 후)
        log.info("⏱️ 최적화 캐시 테스트 시작...");
        historyService.forceEvictUserCurrencyCache(email, currency);
        long optimizedCacheTotal = 0;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            historyService.getNonRegulationHistoryOptimizedCache(email, currency, 0, 20);
            long endTime = System.currentTimeMillis();
            optimizedCacheTotal += (endTime - startTime);

            // 진행률 표시 (25% 단위)
            if ((i + 1) % (iterations / 4) == 0) {
                log.info("최적화 캐시 테스트 진행률: {}% ({}/{})",
                        (i + 1) * 100 / iterations, i + 1, iterations);
            }
        }

        // 결과 계산
        double pureDBAvg = (double) pureDBTotal / iterations;
        double optimizedCacheAvg = (double) optimizedCacheTotal / iterations;

        results.put("email", email);
        results.put("currency", currency);
        results.put("iterations", iterations);
        results.put("dataInfo", Map.of(
                "dataCount", dataCount,
                "hasMoreData", hasMoreData,
                "description", dataCount > 0 ? "실제 거래 데이터 존재" : "거래 데이터 없음"
        ));

        results.put("pureDB", Map.of(
                "totalTime", pureDBTotal,
                "averageTime", Math.round(pureDBAvg * 100) / 100.0,
                "description", "순수 DB 조회 (캐시 없음)"
        ));

        results.put("optimizedCache", Map.of(
                "totalTime", optimizedCacheTotal,
                "averageTime", Math.round(optimizedCacheAvg * 100) / 100.0,
                "description", "최적화된 단일 캐시"
        ));

        // 성능 비교
        double improvement = pureDBAvg > 0 ? ((pureDBAvg - optimizedCacheAvg) / pureDBAvg) * 100 : 0;

        results.put("performance", Map.of(
                "optimizedCacheVsPureDB", Math.round(improvement * 100) / 100.0,
                "speedupFactor", pureDBAvg > 0 ? Math.round((pureDBAvg / optimizedCacheAvg) * 100) / 100.0 : 0,
                "bestMethod", findBestMethod(pureDBAvg, optimizedCacheAvg)
        ));

        log.info("🎯 테스트 완료! 데이터: {}건, 반복: {}회", dataCount, iterations);
        log.info("📊 결과 - 순수DB: {}ms, 최적화캐시: {}ms, 향상률: {}%",
                String.format("%.2f", pureDBAvg),
                String.format("%.2f", optimizedCacheAvg),
                String.format("%.1f", improvement));

        return ResponseEntity.ok(results);
    }

    private String findBestMethod(double pureDB, double optimizedCache) {
        if (pureDB <= optimizedCache) {
            return "순수 DB 조회가 더 빠름";
        } else {
            double speedup = pureDB / optimizedCache;
            return String.format("최적화된 캐시가 %.1f배 더 빠름", speedup);
        }
    }

    /**
     * 캐시 강제 삭제 (테스트용)
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<String> clearCache(
            @RequestParam String currency,
            AppAuthentication auth) {

        String email = auth.getUserEmail();
        historyService.forceEvictUserCurrencyCache(email, currency);

        return ResponseEntity.ok("캐시가 삭제되었습니다 - " + email + ":" + currency);
    }

    /**
     * 캐시 상태 확인
     */
    @GetMapping("/cache-status")
    public ResponseEntity<String> checkCacheStatus(
            @RequestParam String currency,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            AppAuthentication auth) {

        String email = auth.getUserEmail();
        historyService.checkCacheStatus(email, currency, page, size);

        return ResponseEntity.ok("캐시 상태가 로그에 출력되었습니다 - " + email);
    }

    /**
     * 인증 정보 확인 (디버깅용)
     */
    @GetMapping("/auth-info")
    public ResponseEntity<Map<String, Object>> getAuthInfo(AppAuthentication auth) {
        Map<String, Object> authInfo = new HashMap<>();

        authInfo.put("userEmail", auth.getUserEmail());
        authInfo.put("role", auth.getUserRole());
        authInfo.put("authenticated", true);

        return ResponseEntity.ok(authInfo);
    }


}