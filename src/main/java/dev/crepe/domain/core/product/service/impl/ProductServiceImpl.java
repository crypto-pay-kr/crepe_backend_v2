package dev.crepe.domain.core.product.service.impl;

import dev.crepe.domain.admin.dto.request.ChangeProductSaleRequest;
import dev.crepe.domain.admin.dto.response.GetAllProductResponse;
import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.dto.request.ReviewProductSubmissionRequest;
import dev.crepe.domain.core.product.model.dto.response.ReviewProductSubmissionResponse;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<GetAllProductResponse> getAllBankProducts(Long bankId) {
        return getBankProductsByStatus(bankId, false);
    }

    @Override
    public List<GetAllProductResponse> getSuspendedBankProducts(Long bankId) {
        return getBankProductsByStatus(bankId, true);
    }

    private List<GetAllProductResponse> getBankProductsByStatus(Long bankId, boolean includeStatus) {
        List<Product> products = productRepository.findByBankId(bankId);

        return products.stream()
                .filter(product -> includeStatus == (product.getStatus() == BankProductStatus.SUSPENDED))
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    private GetAllProductResponse mapToProductResponse(Product product) {
        float maxInterestRate = product.getBaseInterestRate();
        if (product.getPreferentialConditions() != null && !product.getPreferentialConditions().isEmpty()) {
            float maxAdditionalRate = product.getPreferentialConditions().stream()
                    .map(PreferentialInterestCondition::getRate)
                    .max(Comparator.naturalOrder())
                    .orElse(0f);
            maxInterestRate += maxAdditionalRate;
        }

        return GetAllProductResponse.builder()
                .id(product.getId())
                .type(product.getType())
                .productName(product.getProductName())
                .bankName(product.getBank().getName())
                .totalBudget(product.getBudget())
                .status(product.getStatus())
                .totalParticipants(product.getMaxParticipants())
                .minInterestRate(product.getBaseInterestRate())
                .maxInterestRate(maxInterestRate)
                .build();
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
