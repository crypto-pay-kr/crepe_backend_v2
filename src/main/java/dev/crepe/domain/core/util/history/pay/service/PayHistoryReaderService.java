package dev.crepe.domain.core.util.history.pay.service;

import dev.crepe.domain.admin.dto.response.GetPayHistoryResponse;
import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.channel.market.order.service.OrderService;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.infra.redis.service.RedisHistoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class PayHistoryReaderService {

    private final OrderService orderService;
    private final PayHistoryService payHistoryService;
    private final RedisHistoryService redisHistoryService;
    private final ExceptionDbService exceptionDbService;

    /**
     * 사용자 이메일로 결제 내역 조회 - 캐시 적용
     */

    public Page<GetPayHistoryResponse> getPayHistoriesByUserEmail(String userEmail, String type, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("❌ 캐시 메서드에서 인증 실패 - userEmail: {}", userEmail);
            throw exceptionDbService.getException("ACTOR_001") ;
        }

        log.info("💾 MySQL에서 사용자 결제 내역 조회 - userEmail: {}, type: {}, page: {}",
                userEmail, type, pageable.getPageNumber());

        try {
            Page<Order> orders = orderService.getOrdersByUserEmail(userEmail, pageable);

            // 필터링 및 매핑
            List<GetPayHistoryResponse> filtered = orders.getContent().stream()
                    .map(order -> {
                        PayHistory payHistory = payHistoryService.getPayHistoryByOrderId(order.getId());
                        String detailSummary = order.getOrderDetails().stream()
                                .map(d -> d.getMenu().getName() + "x" + d.getMenuCount())
                                .collect(Collectors.joining(", "));

                        return new GetPayHistoryResponse(
                                payHistory.getId(),
                                order.getId(),
                                order.getStore().getName(),
                                order.getCreatedAt(),
                                detailSummary,
                                payHistory.getTotalAmount(),
                                order.getStore().getNickName(),
                                order.getCurrency(),
                                order.getTotalPrice(),
                                payHistory.getStatus().name()
                        );
                    })
                    .filter(dto -> type == null || dto.getPayType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());

            Page<GetPayHistoryResponse> result = new PageImpl<>(filtered, pageable, filtered.size());

            log.info("✅ 사용자 결제 내역 조회 완료 → Redis 캐싱 - userEmail: {}, 조회 건수: {}",
                    userEmail, result.getTotalElements());

            // 🔥 첫 페이지의 최근 결제 내역을 별도로 빠른 조회용 캐싱
            if (pageable.getPageNumber() == 0 && !filtered.isEmpty()) {
                cacheRecentPayHistory(userEmail, filtered.subList(0, Math.min(10, filtered.size())));
            }

            return result;

        } catch (Exception e) {
            log.error("❌ 사용자 결제 내역 조회 중 오류 - userEmail: {}", userEmail, e);
            throw e;
        }
    }

    /**
     * 최근 결제 내역을 Redis에 별도 저장 (빠른 조회용)
     */
    private void cacheRecentPayHistory(String userEmail, List<GetPayHistoryResponse> recentPayments) {
        String key = "recent:pay:" + userEmail;
        redisHistoryService.cacheHistoryStats(key, recentPayments, Duration.ofMinutes(10));
        log.debug("💨 최근 결제 내역 별도 캐싱 - userEmail: {}, 건수: {}", userEmail, recentPayments.size());
    }

}
