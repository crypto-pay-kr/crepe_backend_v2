package dev.crepe.domain.core.subscribe.model.dto.response;

import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SubscribeResponseDto {
    private Long id; // 가입 ID
    private Long bankTokenId; // 은행 토큰 id
    private String productName; // 상품 이름
    private SubscribeStatus status; // ACTIVE / EXPIRED
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal balance; // 현재 잔액
    private float baseInterestRate; // 기본 이자율
    private String appliedPreferentialRates; // 적용된 우대 금리 설명 (선택)
    private String productType;
    private BigDecimal maxMonthlyPayment;
    private BigDecimal preTaxInterest; // 세전 이자

    public static SubscribeResponseDto from(Subscribe s) {
        return new SubscribeResponseDto(
                s.getId(),
                s.getProduct().getBankToken().getId(),
                s.getProduct().getProductName(),
                s.getStatus(),
                s.getSubscribeDate().toLocalDate(),
                s.getExpiredDate().toLocalDate(),
                s.getBalance(),
                s.getInterestRate(),
                s.getAppliedPreferentialRates(),
                s.getProduct().getType().name(),
                s.getProduct().getMaxMonthlyPayment(),
                s.getPreTaxInterest()
        );
    }
}
