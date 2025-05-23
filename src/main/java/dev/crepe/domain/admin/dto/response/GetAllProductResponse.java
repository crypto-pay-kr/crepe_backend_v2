package dev.crepe.domain.admin.dto.response;

import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.BankProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllProductResponse {
    private Long id;
    private BankProductType type;
    private String productName;      // 상품 이름
    private String bankName;         // 주관은행
    private BigDecimal totalBudget; // 혜택 자본금
    private Integer totalParticipants;
    private BankProductStatus status;
    private Float minInterestRate;
    private Float maxInterestRate;
    private String imageUrl;
    private String guideFile;

}
