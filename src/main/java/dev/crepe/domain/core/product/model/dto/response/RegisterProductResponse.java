package dev.crepe.domain.core.product.model.dto.response;

import dev.crepe.domain.core.product.model.BankProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@AllArgsConstructor
@Builder
public class RegisterProductResponse {
    private Long productId;
    private String productName;
    private BankProductType type;
}
