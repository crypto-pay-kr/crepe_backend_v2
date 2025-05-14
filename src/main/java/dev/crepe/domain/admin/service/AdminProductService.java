package dev.crepe.domain.admin.service;

import dev.crepe.domain.core.product.model.dto.request.ReviewProductSubmissionRequest;
import dev.crepe.domain.core.product.model.dto.response.ReviewProductSubmissionResponse;
import org.springframework.http.ResponseEntity;

public interface AdminProductService {
    ReviewProductSubmissionResponse reviewProductSubmission(ReviewProductSubmissionRequest request);
}
