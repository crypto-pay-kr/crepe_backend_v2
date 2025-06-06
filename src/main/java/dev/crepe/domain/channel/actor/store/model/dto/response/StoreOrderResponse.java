package dev.crepe.domain.channel.actor.store.model.dto.response;

import dev.crepe.domain.channel.market.order.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class StoreOrderResponse {
    private String orderId;
    private String clientOrderNumber;
    private int totalPrice;
    private OrderStatus status;
    private String orderType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime readyAt;
    private List<OrderDetailResponse> orderDetails;

    @Getter
    @Builder
    public static class OrderDetailResponse {
        private String menuName;
        private int menuCount;
        private int menuPrice;
    }
}