package dev.crepe.domain.channel.actor.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GetFinancialSummaryResponse {
    private Long userId;
    private BigDecimal annualIncome; // 연간 소득
    private BigDecimal totalAsset;   // 총 자산
}
