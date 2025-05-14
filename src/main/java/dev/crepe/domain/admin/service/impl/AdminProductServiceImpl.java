package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.service.AdminProductService;
import dev.crepe.domain.core.product.model.dto.request.ReviewProductSubmissionRequest;
import dev.crepe.domain.core.product.model.dto.response.ReviewProductSubmissionResponse;
import dev.crepe.domain.core.product.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {
    private final ProductServiceImpl productService;

    @Override
    public ReviewProductSubmissionResponse reviewProductSubmission(ReviewProductSubmissionRequest request) {
        return productService.productInspect(request);
    }
}
