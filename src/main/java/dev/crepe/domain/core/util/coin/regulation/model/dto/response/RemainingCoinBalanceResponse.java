package dev.crepe.domain.core.util.coin.regulation.model.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemainingCoinBalanceResponse {

    private String coinName;
    private String currency;
    private BigDecimal publishedBalance; // 발행된 금액
    private BigDecimal accountBalance; // 계좌 잔액
    private BigDecimal remainingBalance; // 사용 불가능한 잔액
}
