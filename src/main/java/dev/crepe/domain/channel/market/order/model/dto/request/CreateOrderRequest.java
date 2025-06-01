package dev.crepe.domain.channel.market.order.model.dto.request;

import dev.crepe.domain.core.pay.PaymentType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

    private Long storeId;
    private String userEmail;
    private List<OrderDetailRequest> orderDetails;
    private String currency;
    private BigDecimal exchangeRate;
    private PaymentType paymentType;
    private Long voucherSubscribeId;


    @Getter
    public static class OrderDetailRequest{
        private Long menuId;
        private int menuCount;

        public OrderDetailRequest(Long menuId, int menuCount) {
            this.menuId = menuId;
            this.menuCount = menuCount;
        }
    }

    public CreateOrderRequest(BigDecimal exchangeRate, Long storeId, String userEmail, List<OrderDetailRequest> orderDetails, String currency, PaymentType paymentType, Long voucherSubscribeId) {
        this.storeId = storeId;
        this.userEmail = userEmail;
        this.orderDetails = orderDetails;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.paymentType = paymentType;
        this.voucherSubscribeId = voucherSubscribeId;
    }
}

