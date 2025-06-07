package dev.crepe.domain.bank.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.bank.model.dto.response.GetAllProductResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.bank.service.BankProductService;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.product.model.dto.eligibility.AgeGroup;
import dev.crepe.domain.core.product.model.dto.eligibility.EligibilityCriteria;
import dev.crepe.domain.core.product.model.dto.eligibility.IncomeLevel;
import dev.crepe.domain.core.product.model.dto.eligibility.Occupation;
import dev.crepe.domain.core.product.model.dto.interest.AgePreferentialRate;
import dev.crepe.domain.core.product.model.dto.interest.DepositPreferentialRate;
import dev.crepe.domain.core.product.model.dto.interest.FreeDepositCountPreferentialRate;
import dev.crepe.domain.core.product.model.dto.request.EligibilityCriteriaDto;
import dev.crepe.domain.core.product.model.dto.request.PreferentialRateConditionDto;
import dev.crepe.domain.core.product.model.dto.request.RegisterProductRequest;
import dev.crepe.domain.core.product.model.dto.response.GetPreferentialConditionResponse;
import dev.crepe.domain.core.product.model.dto.response.RegisterProductResponse;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.model.entity.Tag;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.product.repository.TagRepository;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.infra.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class BankProductServiceImpl implements BankProductService {
    private final BankTokenRepository bankTokenRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final BankRepository bankRepository;
    private final S3Service s3Service;
    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final MaxParticipantsCalculatorService maxParticipantsCalculatorService;
    private final ExceptionDbService exceptionDbService;

    @Override
    public RegisterProductResponse registerProduct(String email, MultipartFile productImage, MultipartFile guideFile, RegisterProductRequest request) {
        log.info("상품 등록 요청: email={}, request={}", email, request);
        Bank bank = bankRepository.findByEmail(email)
                .orElseThrow(() -> exceptionDbService.getException("BANK_001"));

        BankToken bankToken = bankTokenRepository.findByBank(bank)
                .orElseThrow(() -> exceptionDbService.getException("BANK_TOKEN_001"));

        Account tokenAccount = accountRepository.findByBankAndBankToken(bank, bankToken)
                .orElseThrow(() -> exceptionDbService.getException("ACCOUNT_001"));


        BigDecimal budget = request.getBudget();

        if (tokenAccount.getBalance().compareTo(budget) < 0) {
            throw exceptionDbService.getException("ACCOUNT_006");
        }


        accountService.validateAndDeductBalance(tokenAccount, budget);
        tokenAccount.addNonAvailableBalance(budget);

        accountRepository.save(tokenAccount);

        String productImageUrl = s3Service.uploadFile(productImage, "product-images");

        String contentType = guideFile.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw exceptionDbService.getException("S3_UPLOAD_001");
        }
        String guideFileUrl = s3Service.uploadFile(guideFile, "product-guides");

        EligibilityCriteria eligibilityCriteria = buildEligibilityCriteria(request.getEligibilityCriteria());
        String joinConditionJson = convertEligibilityCriteriaToString(eligibilityCriteria);

        // 자동계산
        Integer calculatedMaxParticipants = maxParticipantsCalculatorService.calculateMaxParticipants(
                request.getType(),
                budget,
                request.getMaxMonthlyPayment(),
                request.getBaseRate().floatValue(),
                request.getStartDate(),
                request.getEndDate()
        );

        boolean isVoucher = BankProductType.VOUCHER.equals(request.getType());
        if (isVoucher && request.getStoreType() == null) {
            throw exceptionDbService.getException("PRODUCT_002");
        }

        // 상품 엔티티 생성
        Product product = Product.builder()
                .bank(bank)
                .bankToken(bankToken)
                .productName(request.getProductName())
                .type(request.getType())
                .status(BankProductStatus.WAITING)
                .description(request.getDescription())
                .rejectionReason(null)
                .budget(budget)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .storeType(isVoucher ? request.getStoreType() : null)
                .joinCondition(joinConditionJson)
                .baseInterestRate(request.getBaseRate().floatValue())
                .maxMonthlyPayment(request.getMaxMonthlyPayment())
                .maxParticipants(calculatedMaxParticipants)
                .imageUrl(productImageUrl)
                .guideFileUrl(guideFileUrl)
                .build();


        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            List<Tag> tags = getOrCreateTags(request.getTagNames());
            tags.forEach(product::addTag);
        }

        if (request.getPreferentialRateCondition() != null) {
            PreferentialRateConditionDto conditionDto = request.getPreferentialRateCondition();

            // 연령별 우대금리들 처리 (여러 개 가능)
            List<AgePreferentialRate> ageRates = conditionDto.getAgeRates();
            for (AgePreferentialRate ageRate : ageRates) {
                PreferentialInterestCondition condition = PreferentialInterestCondition.builder()
                        .title(ageRate.getName())
                        .description(ageRate.getDescription())
                        .rate(ageRate.getRate().floatValue())
                        .build();
                product.addPreferentialCondition(condition);
            }

            // 예치금액별 우대금리들 처리 (여러 개 가능)
            List<DepositPreferentialRate> depositRates = conditionDto.getDepositRates();
            for (DepositPreferentialRate depositRate : depositRates) {
                PreferentialInterestCondition condition = PreferentialInterestCondition.builder()
                        .title(depositRate.getName())
                        .description(depositRate.getDescription())
                        .rate(depositRate.getRate().floatValue())
                        .build();
                product.addPreferentialCondition(condition);
            }

            // 자유납입별 우대금리들 처리 (여러 개 가능)
            List<FreeDepositCountPreferentialRate> freeRates = conditionDto.getFreeDepositCountRates();
            for (FreeDepositCountPreferentialRate freeRate : freeRates) {
                PreferentialInterestCondition condition = PreferentialInterestCondition.builder()
                        .title(freeRate.getName())
                        .description(freeRate.getDescription())
                        .rate(freeRate.getRate().floatValue())
                        .build();
                product.addPreferentialCondition(condition);
            }
        }


        Product saved = productRepository.save(product);

        RegisterProductResponse.RegisterProductResponseBuilder responseBuilder = RegisterProductResponse.builder()
                .productId(saved.getId())
                .productName(saved.getProductName())
                .type(saved.getType());

        if (BankProductType.VOUCHER.equals(saved.getType())) {
            responseBuilder.storeType(saved.getStoreType());
        }

        return responseBuilder.build();

    }

    private EligibilityCriteria buildEligibilityCriteria(EligibilityCriteriaDto selection) {
        // 선택이 없는 경우 모든 대상 가능으로 설정
        if (selection == null) {
            return EligibilityCriteria.createForAll();
        }

        List<AgeGroup> ageGroups = new ArrayList<>();
        if (selection.getAgeGroups() != null && !selection.getAgeGroups().isEmpty()) {
            for (String code : selection.getAgeGroups()) {
                try {
                    ageGroups.add(AgeGroup.valueOf(code));
                } catch (IllegalArgumentException e) {
                    log.warn("잘못된 연령대 코드: {}", code);
                }
            }
        } else {
            // 선택이 없으면 ALL_AGES 추가
            ageGroups.add(AgeGroup.ALL_AGES);
        }

        List<Occupation> occupations = new ArrayList<>();
        if (selection.getOccupations() != null && !selection.getOccupations().isEmpty()) {
            for (String code : selection.getOccupations()) {
                try {
                    Occupation occupation = Occupation.valueOf(code);
                    // ALL_OCCUPATIONS가 포함되어 있으면 모든 직업 추가
                    if (occupation == Occupation.ALL_OCCUPATIONS) {
                        occupations.clear();
                        occupations.add(Occupation.ALL_OCCUPATIONS);
                        break;
                    }
                    occupations.add(occupation);
                } catch (IllegalArgumentException e) {
                    log.warn("잘못된 직업 코드: {}", code);
                }
            }
        } else {
            occupations.add(Occupation.ALL_OCCUPATIONS);
        }

        List<IncomeLevel> incomeLevels = new ArrayList<>();
        if (selection.getIncomeLevels() != null && !selection.getIncomeLevels().isEmpty()) {
            for (String code : selection.getIncomeLevels()) {
                try {
                    incomeLevels.add(IncomeLevel.valueOf(code));
                } catch (IllegalArgumentException e) {
                    log.warn("잘못된 소득 수준 코드: {}", code);
                }
            }
        } else {
            incomeLevels.add(IncomeLevel.NO_LIMIT);
        }

        return EligibilityCriteria.builder()
                .ageGroups(ageGroups)
                .occupations(occupations)
                .incomeLevels(incomeLevels)
                .build();
    }

    // 가입 조건을 JSON 문자열로 변환
    private String convertEligibilityCriteriaToString(EligibilityCriteria criteria) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(criteria);
        } catch (JsonProcessingException e) {
            log.error("가입 조건 직렬화 오류", e);
            return "{}";
        }
    }



    public List<Tag> getOrCreateTags(List<String> tagNames) {
        List<Tag> tags = new ArrayList<>();

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag(tagName);
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }

        return tags;
    }

    @Override
    public List<GetAllProductResponse> findAllProductsByBankEmail(String email) {
        log.info("은행 이메일로 상품 조회 요청: email={}", email);

        Bank bank = bankRepository.findByEmail(email)
                .orElseThrow(() -> exceptionDbService.getException("BANK_001"));

        List<Product> products = productRepository.findByBank(bank);

        return products.stream()
                .map(product -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<String> parsedJoinConditions = new ArrayList<>();

                    try {
                        // JSON 문자열을 Map으로 변환
                        Map<String, Object> joinConditionMap = objectMapper.readValue(product.getJoinCondition(), new TypeReference<>() {});

                        // Map의 값을 배열 형태로 변환
                        joinConditionMap.forEach((key, value) -> {
                            if (value instanceof List) {
                                parsedJoinConditions.add(key + ": " + value);
                            } else {
                                parsedJoinConditions.add(key + ": " + value.toString());
                            }
                        });
                    } catch (JsonProcessingException e) {
                        log.warn("JSON 파싱 오류: {}", e.getMessage());
                    }

                    return GetAllProductResponse.builder()
                            .id(product.getId())
                            .productName(product.getProductName())
                            .type(product.getType().name())
                            .status(product.getStatus())
                            .description(product.getDescription())
                            .rejectReason(product.getRejectionReason())
                            .budget(product.getBudget())
                            .startDate(product.getStartDate())
                            .endDate(product.getEndDate())
                            .storeType(product.getType().equals(BankProductType.VOUCHER) ? product.getStoreType() : null)
                            .baseInterestRate(product.getBaseInterestRate())
                            .maxMonthlyPayment(product.getMaxMonthlyPayment())
                            .maxParticipants(product.getMaxParticipants())
                            .imageUrl(product.getImageUrl())
                            .guideFileUrl(product.getGuideFileUrl())
                            .tags(product.getProductTags().stream()
                                    .map(productTag -> productTag.getTag().getName())
                                    .toList())
                            .preferentialConditions(
                                    product.getPreferentialConditions().stream()
                                            .map(cond -> GetPreferentialConditionResponse.builder()
                                                    .title(cond.getTitle())
                                                    .description(cond.getDescription())
                                                    .rate(cond.getRate())
                                                    .build())
                                            .toList())
                            .joinConditions(parsedJoinConditions.toString()) // 배열 형태로 변환된 값
                            .build();
                })
                .toList();
    }


    @Override
    public GetAllProductResponse findProductByIdAndBankEmail(String email, Long productId) {
        Bank bank = bankRepository.findByEmail(email)
                .orElseThrow(() -> exceptionDbService.getException("BANK_001"));

        Product product = productRepository.findByIdAndBank(productId, bank)
                .orElseThrow(() -> exceptionDbService.getException("PRODUCT_001"));

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> parsedJoinConditions = new ArrayList<>();

        try {
            Map<String, Object> joinConditionMap = objectMapper.readValue(product.getJoinCondition(), new TypeReference<>() {});
            joinConditionMap.forEach((key, value) -> {
                if (value instanceof List) {
                    parsedJoinConditions.add(key + ": " + value);
                } else {
                    parsedJoinConditions.add(key + ": " + value.toString());
                }
            });
        } catch (JsonProcessingException e) {
            log.warn("JSON 파싱 오류: {}", e.getMessage());
        }

        return GetAllProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .type(product.getType().name())
                .status(product.getStatus())
                .description(product.getDescription())
                .rejectReason(product.getRejectionReason())
                .budget(product.getBudget())
                .startDate(product.getStartDate())
                .endDate(product.getEndDate())
                .baseInterestRate(product.getBaseInterestRate())
                .maxMonthlyPayment(product.getMaxMonthlyPayment())
                .maxParticipants(product.getMaxParticipants())
                .imageUrl(product.getImageUrl())
                .guideFileUrl(product.getGuideFileUrl())
                .tags(product.getProductTags().stream()
                        .map(productTag -> productTag.getTag().getName())
                        .toList())
                .preferentialConditions(
                        product.getPreferentialConditions().stream()
                                .map(cond -> GetPreferentialConditionResponse.builder()
                                        .title(cond.getTitle())
                                        .description(cond.getDescription())
                                        .rate(cond.getRate())
                                        .build())
                                .toList())
                .joinConditions(parsedJoinConditions.toString())
                .build();
    }

    @Override
    public List<GetAllProductResponse> findSuspendedProductsByBankEmail(String email) {
        Bank bank = bankRepository.findByEmail(email)
                .orElseThrow(() -> exceptionDbService.getException("BANK_001"));

        List<Product> suspendedProducts = productRepository.findByBankAndStatus(bank, BankProductStatus.SUSPENDED);

        return suspendedProducts.stream()
                .map(product -> GetAllProductResponse.builder()
                        .id(product.getId())
                        .productName(product.getProductName())
                        .type(product.getType().name())
                        .status(product.getStatus())
                        .description(product.getDescription())
                        .rejectReason(product.getRejectionReason())
                        .budget(product.getBudget())
                        .startDate(product.getStartDate())
                        .endDate(product.getEndDate())
                        .baseInterestRate(product.getBaseInterestRate())
                        .maxMonthlyPayment(product.getMaxMonthlyPayment())
                        .maxParticipants(product.getMaxParticipants())
                        .imageUrl(product.getImageUrl())
                        .guideFileUrl(product.getGuideFileUrl())
                        .tags(product.getProductTags().stream()
                                .map(productTag -> productTag.getTag().getName())
                                .toList())
                        .preferentialConditions(
                                product.getPreferentialConditions().stream()
                                        .map(cond -> GetPreferentialConditionResponse.builder()
                                                .title(cond.getTitle())
                                                .description(cond.getDescription())
                                                .rate(cond.getRate())
                                                .build())
                                        .toList())
                        .joinConditions(product.getJoinCondition())
                        .build())
                .toList();
    }



    @Override
    public List<String> getAllProductsTags() {
        return tagRepository.findAll().stream()
                .map(Tag::getName)
                .toList();
    }
}
