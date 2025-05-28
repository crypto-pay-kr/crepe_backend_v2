package dev.crepe.domain.core.product.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.admin.dto.request.ChangeProductSaleRequest;
import dev.crepe.domain.admin.dto.response.GetAllProductResponse;
import dev.crepe.domain.admin.dto.response.GetProductDetailResponse;
import dev.crepe.domain.admin.dto.response.JoinConditionDto;
import dev.crepe.domain.admin.dto.response.PreferentialConditionDto;
import dev.crepe.domain.channel.actor.model.dto.request.ActorEligibilityRequest;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.dto.eligibility.AgeGroup;
import dev.crepe.domain.core.product.model.dto.eligibility.EligibilityCriteria;
import dev.crepe.domain.core.product.model.dto.eligibility.IncomeLevel;
import dev.crepe.domain.core.product.model.dto.eligibility.Occupation;
import dev.crepe.domain.core.product.model.dto.request.ReviewProductSubmissionRequest;
import dev.crepe.domain.core.product.model.dto.response.GetOnsaleProductListReponse;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
                .bankName(product.getBank().getName())
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

    @Override
    public List<GetOnsaleProductListReponse> getOnSaleProducts() {
        // 판매 중인 상품 조회
        List<Product> products = productRepository.findByStatus(BankProductStatus.APPROVED);

        return products.stream()
                .map(product -> {
                    // 현재 참여자 수 계산
                    int currentParticipants = subscribeRepository.countByProductIdAndStatus(product.getId(), SubscribeStatus.ACTIVE);

                    // 남은 자본금 수량 계산
                    BigDecimal usedBudget = subscribeRepository.sumAmountByProductId(product.getId());
                    BigDecimal remainingBudget = product.getBudget().subtract(usedBudget != null ? usedBudget : BigDecimal.ZERO);

                    // 마감 기한
                    String deadline = product.getEndDate() != null ? product.getEndDate().toString() : null;

                    // 응답 객체 생성
                    return GetOnsaleProductListReponse.builder()
                            .id(product.getId())
                            .type(product.getType())
                            .productName(product.getProductName())
                            .bankName(product.getBank().getName())
                            .totalBudget(product.getBudget())
                            .remainingBudget(remainingBudget)
                            .totalParticipants(product.getMaxParticipants())
                            .currentParticipants(currentParticipants)
                            .status(product.getStatus())
                            .minInterestRate(product.getBaseInterestRate())
                            .maxInterestRate(calculateMaxInterestRate(product))
                            .imageUrl(product.getImageUrl())
                            .tags(extractTags(product.getProductTags()))
                            .guideFile(product.getGuideFileUrl())
                            .deadline(deadline)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 최대 금리 계산
    private Float calculateMaxInterestRate(Product product) {
        float maxInterestRate = product.getBaseInterestRate();
        if (product.getPreferentialConditions() != null && !product.getPreferentialConditions().isEmpty()) {
            float maxAdditionalRate = product.getPreferentialConditions().stream()
                    .map(PreferentialInterestCondition::getRate)
                    .max(Comparator.naturalOrder())
                    .orElse(0f);
            maxInterestRate += maxAdditionalRate;
        }
        return maxInterestRate;
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
    @Override
    public boolean checkEligibility(Long productId, ActorEligibilityRequest actorEligibilityRequest) {
        // Product 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product를 찾을 수 없습니다."));

        // JoinCondition 파싱
        EligibilityCriteria criteria;
        try {
            criteria = objectMapper.readValue(product.getJoinCondition(), EligibilityCriteria.class);
        } catch (IOException e) {
            throw new IllegalStateException("상품 가입 조건을 확인할 수 없습니다.");
        }

        // 조건 비교
        boolean ageMatch = criteria.getAgeGroups().contains(AgeGroup.ALL_AGES) ||
                criteria.getAgeGroups().contains(actorEligibilityRequest.getAgeGroup());
        boolean occupationMatch = criteria.getOccupations().contains(Occupation.ALL_OCCUPATIONS) ||
                criteria.getOccupations().contains(actorEligibilityRequest.getOccupation());
        boolean incomeMatch = criteria.getIncomeLevels().contains(IncomeLevel.NO_LIMIT) ||
                criteria.getIncomeLevels().contains(actorEligibilityRequest.getIncomeLevel());

        // 모든 조건이 true여야 true 반환
        return ageMatch && occupationMatch && incomeMatch;
    }

}
