package dev.crepe.domain.core.product.model.dto.response;

import dev.crepe.domain.core.product.model.BankProductStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReviewProductSubmissionResponse {
    private Long productId;
    private BankProductStatus status;
    private String productName;
    private String message;
}
