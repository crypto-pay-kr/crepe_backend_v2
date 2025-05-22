package dev.crepe.domain.core.product.model.dto.request;

import dev.crepe.domain.core.product.model.BankProductStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewProductSubmissionRequest {
    private Long productId;
    private BankProductStatus status;
    private String description;

    @Builder
    public ReviewProductSubmissionRequest(Long productId, BankProductStatus status, String description) {
        this.productId = productId;
        this.status = status;
        this.description = description;
    }
}
