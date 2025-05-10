package dev.crepe.domain.channel.market.order.model.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class CreateOrderRequest {

    private Long storeId;
    private String userEmail;
    private List<OrderDetailRequest> orderDetails;
    private String currency;


    @Getter
    public static class OrderDetailRequest{
        private Long menuId;
        private int menuCount;

        public OrderDetailRequest(Long menuId, int menuCount) {
            this.menuId = menuId;
            this.menuCount = menuCount;
        }
    }

    public CreateOrderRequest(Long storeId, String userEmail, List<OrderDetailRequest> orderDetails, String currency) {
        this.storeId = storeId;
        this.userEmail = userEmail;
        this.orderDetails = orderDetails;
        this.currency = currency;
    }
}

