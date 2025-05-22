package dev.crepe.domain.channel.actor.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetFinancialSummaryResponse {
    private Long userId;
    private BigDecimal annualIncome; // 연간 소득
    private BigDecimal totalAsset;   // 총 자산
}
