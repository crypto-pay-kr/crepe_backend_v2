package dev.crepe.domain.channel.market.order.model.dto.response;

import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.channel.market.order.model.entity.OrderDetail;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CreateOrderResponse {

    private String orderId;
    private int totalPrice;
    private String orderStatus;
    private String orderType;
    private String storeName;
    private String storeAddress;
    private LocalDateTime createdAt;
    private List<OrderDetailResponse> orderDetails;

    public static CreateOrderResponse from(Order order) {
        return CreateOrderResponse.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getStatus().name())
                .orderType(order.getType().name())
                .storeName(order.getStore().getName())
                .storeAddress(order.getStore().getStoreAddress())
                .createdAt(order.getCreatedAt())
                .orderDetails(order.getOrderDetails().stream()
                        .map(OrderDetailResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }

    @Getter
    @Builder
    public static class OrderDetailResponse {
        private String menuName;
        private int menuCount;
        private int menuPrice;

        public static OrderDetailResponse from(OrderDetail orderDetail) {
            return OrderDetailResponse.builder()
                    .menuName(orderDetail.getMenu().getName())
                    .menuCount(orderDetail.getMenuCount())
                    .menuPrice(orderDetail.getMenu().getPrice())
                    .build();
        }
    }
}