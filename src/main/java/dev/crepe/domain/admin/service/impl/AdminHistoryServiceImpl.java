package dev.crepe.domain.admin.service.impl;


import dev.crepe.domain.admin.dto.response.GetPayHistoryResponse;
import dev.crepe.domain.admin.dto.response.GetSettlementHistoryResponse;
import dev.crepe.domain.admin.service.AdminHistoryService;
import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.channel.market.order.service.OrderService;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import dev.crepe.domain.core.util.history.pay.service.PayHistoryService;
import dev.crepe.infra.redis.service.RedisHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminHistoryServiceImpl implements AdminHistoryService {

    private final OrderService orderService;
    private final PayHistoryService payHistoryService;
    private final TransactionHistoryService transactionHistoryService;
    private final RedisHistoryService redisHistoryService;

    // 유저 ID 기반으로 모든 결제 내역 조회
    @Override
    @Cacheable(
            value = "payHistory",
            key = "#userId + ':' + (#type != null ? #type : 'ALL') + ':' + #pageable.pageNumber + ':' + #pageable.pageSize",
            unless = "#result == null or #result.isEmpty()"
    )
    public Page<GetPayHistoryResponse> getPayHistoriesByUserId(Long userId, String type, Pageable pageable) {
        Page<Order> orders = orderService.getOrdersByUserId(userId, pageable);

        // 1. 필요한 Order만 필터링
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

        return new PageImpl<>(filtered, pageable, filtered.size());
    }


    @Override
    public Page<GetSettlementHistoryResponse> getSettlementHistoriesByUserId(Long storeId, TransactionStatus status, Pageable pageable) {

        Page<TransactionHistory> transactionHistories = transactionHistoryService.getSettlementHistory(status, storeId, pageable);

        return transactionHistories.map(settlement ->
                GetSettlementHistoryResponse.builder()
                        .id(settlement.getId())
                        .date(settlement.getUpdatedAt())
                        .status(settlement.getStatus().name())
                        .currency(settlement.getAccount().getCoin().getCurrency())
                        .amount(settlement.getAmount())
                        .build()
        );
    }


    @Override
    public void reSettlement(Long historyId) {
        transactionHistoryService.reSettlement(historyId);
    }

}



