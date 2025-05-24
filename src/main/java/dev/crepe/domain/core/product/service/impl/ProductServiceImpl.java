package dev.crepe.domain.core.product.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.admin.dto.request.ChangeProductSaleRequest;
import dev.crepe.domain.admin.dto.response.GetAllProductResponse;
import dev.crepe.domain.admin.dto.response.GetProductDetailResponse;
import dev.crepe.domain.admin.dto.response.JoinConditionDto;
import dev.crepe.domain.admin.dto.response.PreferentialConditionDto;
import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.dto.eligibility.EligibilityCriteria;
import dev.crepe.domain.core.product.model.dto.request.ReviewProductSubmissionRequest;
import dev.crepe.domain.core.product.model.dto.response.ReviewProductSubmissionResponse;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.model.entity.ProductTag;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.product.service.ProductService;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
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
    private final SubscribeRepository subscribeRepository;
    private final ObjectMapper objectMapper;

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

    @Override
    public GetProductDetailResponse getProductDetail(Long bankId, Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);
        Integer subscribeCount = subscribeRepository.countByProductIdAndStatus(productId, SubscribeStatus.ACTIVE);

        return GetProductDetailResponse.builder()
                .productName(product.getProductName())
                .type(product.getType())
                .baseInterestRate(product.getBaseInterestRate())
                .joinCondition(parseJoinCondition(product.getJoinCondition()))
                .maxParticipants(product.getMaxParticipants())
                .maxMonthlyPayment(product.getMaxMonthlyPayment())
                .rateConditions(convertToRateConditionDtos(product.getPreferentialConditions()))
                .subscribeCount(subscribeCount)
                .guideFile(product.getGuideFileUrl())
                .imageUrl(product.getImageUrl())
                .budget(product.getBudget())
                .tags(extractTags(product.getProductTags()))
                .startDate(product.getStartDate())
                .endDate(product.getEndDate())
                .build();
    }


    private List<GetAllProductResponse> getBankProductsByStatus(Long bankId, boolean includeStatus) {
        List<Product> products = productRepository.findByBankId(bankId);

        return products.stream()
                .filter(product -> includeStatus == (product.getStatus() == BankProductStatus.SUSPENDED))
                .sorted((p1,p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
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
                .imageUrl(product.getImageUrl())
                .guideFile(product.getGuideFileUrl())
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

    private List<PreferentialConditionDto> convertToRateConditionDtos(
            List<PreferentialInterestCondition> conditions) {
        return conditions.stream()
                .map(condition -> PreferentialConditionDto.builder()
                        .id(condition.getId())
                        .title(condition.getTitle())
                        .rate(condition.getRate())
                        .description(condition.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> extractTags(List<ProductTag> productTags) {
        return productTags.stream()
                .map(productTag -> productTag.getTag().getName())
                .collect(Collectors.toList());
    }
    private JoinConditionDto parseJoinCondition(String joinConditionJson) {
        try {
            // 기존 EligibilityCriteria 사용하거나 직접 파싱
            EligibilityCriteria criteria = objectMapper.readValue(joinConditionJson, EligibilityCriteria.class);

            return JoinConditionDto.builder()
                    .ageGroups(criteria.getAgeGroups().stream()
                            .map(Enum::name)
                            .collect(Collectors.toList()))
                    .occupations(criteria.getOccupations().stream()
                            .map(Enum::name)
                            .collect(Collectors.toList()))
                    .incomeLevels(criteria.getIncomeLevels().stream()
                            .map(Enum::name)
                            .collect(Collectors.toList()))
                    .allAges(criteria.isAllAges())
                    .build();
        } catch (JsonProcessingException e) {
            return JoinConditionDto.builder()
                    .ageGroups(List.of("정보 없음"))
                    .occupations(List.of("정보 없음"))
                    .incomeLevels(List.of("정보 없음"))
                    .allAges(false)
                    .build();
        }
    }
}
