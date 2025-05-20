package dev.crepe.domain.core.product.service.impl;

import dev.crepe.domain.admin.dto.request.ChangeProductSaleRequest;
import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.dto.request.ReviewProductSubmissionRequest;
import dev.crepe.domain.core.product.model.dto.response.ReviewProductSubmissionResponse;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ReviewProductSubmissionResponse productInspect(ReviewProductSubmissionRequest request) {
        return updateProductStatus(request.getProductId(), request.getStatus(), request.getDescription());
    }

    @Override
    @Transactional
    public ReviewProductSubmissionResponse changeProductSalesStatus(ChangeProductSaleRequest request) {
        return updateProductStatus(request.getProductId(), request.getStatus(), request.getDescription());
    }

    private ReviewProductSubmissionResponse updateProductStatus(Long productId, BankProductStatus newStatus, String description) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + productId));

        product.updateStatus(newStatus, description);

        ReviewProductSubmissionResponse.ReviewProductSubmissionResponseBuilder builder =
                ReviewProductSubmissionResponse.builder()
                        .productId(product.getId())
                        .productName(product.getProductName())
                        .status(product.getStatus());


        if ((product.getStatus() == BankProductStatus.REJECTED || product.getStatus() == BankProductStatus.SUSPENDED) &&
                product.getRejectionReason() != null &&
                !product.getRejectionReason().trim().isEmpty()) {
            builder.message(product.getRejectionReason());
        }
        return builder.build();
    }
}
