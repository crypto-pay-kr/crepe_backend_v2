package dev.crepe.domain.core.subscribe.model.dto.request;

import dev.crepe.domain.core.product.model.dto.interest.FreeDepositCountPreferentialRate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscribeProductRequest {
    @Schema(description = "상품 ID", required = true, example = "1")
    private Long productId;

    // 적금/예금용 필드들
    @Schema(description = "초기 납입액 (예치금액 우대금리 계산용)", example = "1000000")
    private BigDecimal initialDepositAmount;

    @Schema(description = "선택한 자유납입 우대금리 목표",
            example = "LEVEL2",
            allowableValues = {"NONE", "LEVEL1", "LEVEL2", "LEVEL3"})
    private FreeDepositCountPreferentialRate selectedFreeDepositRate;

    // 상품권용 필드들 (필요시)
    @Schema(description = "상품권 수량", example = "5")
    private Integer voucherQuantity;

    // 추가 정보 (선택사항)
    @Schema(description = "가입 목적", example = "결혼자금 마련")
    private String purpose;
}