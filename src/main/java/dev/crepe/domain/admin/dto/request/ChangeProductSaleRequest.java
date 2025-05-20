package dev.crepe.domain.admin.dto.request;

import dev.crepe.domain.core.product.model.BankProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "은행 상품 판매 정지, 해제 DTO")
public class ChangeProductSaleRequest {
    private Long productId;
    private BankProductStatus status;
    private String description;
}
