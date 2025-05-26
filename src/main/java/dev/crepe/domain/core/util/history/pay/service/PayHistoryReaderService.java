package dev.crepe.domain.core.util.history.pay.service;

import dev.crepe.domain.admin.dto.response.GetPayHistoryResponse;
import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.channel.market.order.service.OrderService;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PayHistoryReaderService {

    private final OrderService orderService;
    private final PayHistoryService payHistoryService;

    public Page<GetPayHistoryResponse> getPayHistoriesByUserEmail(String userEmail, String type, Pageable pageable) {
        Page<Order> orders = orderService.getOrdersByUserEmail(userEmail, pageable);

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
                            order.getCurrency(),
                            order.getTotalPrice(),
                            payHistory.getStatus().name()
                    );
                })
                .filter(dto -> type == null || dto.getPayType().equalsIgnoreCase(type))
                .collect(Collectors.toList());

        return new PageImpl<>(filtered, pageable, filtered.size());
    }

}
