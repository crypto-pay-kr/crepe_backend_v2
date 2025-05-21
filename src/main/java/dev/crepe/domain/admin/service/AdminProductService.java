package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.request.ChangeProductSaleRequest;
import dev.crepe.domain.core.product.model.dto.request.ReviewProductSubmissionRequest;
import dev.crepe.domain.core.product.model.dto.response.ReviewProductSubmissionResponse;
import org.springframework.stereotype.Service;

@Service
public interface AdminProductService {
    ReviewProductSubmissionResponse reviewProductSubmission(ReviewProductSubmissionRequest request);
    ReviewProductSubmissionResponse changeProductSalesStatus(ChangeProductSaleRequest request);
}
