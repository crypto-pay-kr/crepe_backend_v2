package dev.crepe.domain.admin.dto.response;

import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.product.model.dto.request.PreferentialRateConditionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetProductDetailResponse {
    private Long id;
    private String productName;
    private BankProductType type;
    private Float baseInterestRate;
    private JoinConditionDto joinCondition;
    private Integer maxParticipants;
    private BigDecimal maxMonthlyPayment;
    private List<PreferentialConditionDto> rateConditions;
    private String guideFile;
    private String imageUrl;
    private BigDecimal budget;
    private List<String> tags;
    private Integer subscribeCount;
    private LocalDate startDate;
    private LocalDate endDate;
}


