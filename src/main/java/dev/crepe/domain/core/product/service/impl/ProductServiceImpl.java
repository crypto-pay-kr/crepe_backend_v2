package dev.crepe.domain.core.product.service.impl;

import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.dto.request.ReviewProductSubmissionRequest;
import dev.crepe.domain.core.product.model.dto.response.ReviewProductSubmissionResponse;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    @Override
    public ReviewProductSubmissionResponse productInspect(ReviewProductSubmissionRequest request) {
        Product product = productRepository.findById(Long.valueOf(request.getProductId()))
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + request.getProductId()));

        // 상품 상태 업데이트
        BankProductStatus newStatus = request.getStatus();
        product.updateStatus(newStatus, request.getDescription());

        // 상품 저장
        Product updatedProduct = productRepository.save(product);

        ReviewProductSubmissionResponse.ReviewProductSubmissionResponseBuilder builder =
                ReviewProductSubmissionResponse.builder()
                        .productId(updatedProduct.getId())
                        .productName(updatedProduct.getProductName())
                        .status(updatedProduct.getStatus());

        if (updatedProduct.getStatus() == BankProductStatus.REJECTED &&
                updatedProduct.getRejectionReason() != null &&
                !updatedProduct.getRejectionReason().trim().isEmpty()) {
            builder.message(updatedProduct.getRejectionReason());
        }
        return builder.build();
    }
}
