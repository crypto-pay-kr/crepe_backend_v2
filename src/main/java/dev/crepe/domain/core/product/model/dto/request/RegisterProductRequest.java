package dev.crepe.domain.core.product.model.dto.request;

import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.product.model.entity.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterProductRequest {
   @Schema(description = "상품명", example = "크레페 청년 적금")
   private String productName;

   @Schema(description = "상품 유형", example = "INSTALLMENT")
   private BankProductType type;

   @Schema(description = "가입 자격 조건")
   private EligibilityCriteriaDto eligibilityCriteria;

   @Schema(description = "상품 예산 (총 한도)", example = "100000")
   private BigDecimal budget;

   @Schema(description = "기본 금리 (%)", example = "3.5")
   private BigDecimal baseRate;

   @Schema(description = "최대 월 납입액", example = "1000000")
   private BigDecimal maxMonthlyPayment;

   @Schema(description = "우대 금리 조건")
   private PreferentialRateConditionDto preferentialRateCondition;

   @Schema(description = "상품 시작일", example = "2025-05-18")
   private LocalDate startDate;

   @Schema(description = "상품 종료일", example = "2028-12-31")
   private LocalDate endDate;

   @Schema(description = "상품 태그 목록" ,example = "[\"청년특화\", \"고금리\", \"자유적금\"]")
   private List<String> tagNames;

   @Schema(description = "상품 설명", example = "청년층을 위한 특별 우대금리 적금 상품입니다.")
   private String description;
}
