package dev.crepe.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GetPayHistoryResponse {

    private Long payId;
    private String orderId;
    private String storeName;
    private LocalDateTime payDate;
    private String orderDetail;
    private BigDecimal payCoinAmount;
    private String storeNickname;
    private String coinCurrency;
    private int payKRWAmount;
    private String payType;



}
