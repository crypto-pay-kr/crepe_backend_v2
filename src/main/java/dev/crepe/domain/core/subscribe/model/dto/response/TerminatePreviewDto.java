package dev.crepe.domain.core.subscribe.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TerminatePreviewDto {
    private Long subscribeId;
    private BigDecimal balance; // 원금
    private BigDecimal preTaxInterest; // 세전 이자
    private BigDecimal postTaxInterest; // 세후 이자
    private BigDecimal totalPayout; // 원금 + 세후 이자
    private BigDecimal interestRate; // 기본 금리
}
