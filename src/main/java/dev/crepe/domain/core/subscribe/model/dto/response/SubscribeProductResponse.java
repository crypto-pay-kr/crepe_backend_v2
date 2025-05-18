package dev.crepe.domain.core.subscribe.model.dto.response;

import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SubscribeProductResponse {
    private Long subscribeId;
    private String productName;
    private BankProductType productType;
    private SubscribeStatus status;
    private LocalDateTime subscribeDate;
    private LocalDateTime expiredDate;
    private BigDecimal balance;
    private float interestRate;
    private String message;

    // 상품권 상품인 경우만 반환
    private String voucherCode;
}