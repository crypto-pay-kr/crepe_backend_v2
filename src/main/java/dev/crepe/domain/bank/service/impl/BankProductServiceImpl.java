package dev.crepe.domain.bank.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.bank.service.BankProductService;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.product.exception.InsufficientCapitalException;
import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.dto.eligibility.AgeGroup;
import dev.crepe.domain.core.product.model.dto.eligibility.EligibilityCriteria;
import dev.crepe.domain.core.product.model.dto.eligibility.IncomeLevel;
import dev.crepe.domain.core.product.model.dto.eligibility.Occupation;
import dev.crepe.domain.core.product.model.dto.interest.*;
import dev.crepe.domain.core.product.model.dto.request.EligibilityCriteriaDto;
import dev.crepe.domain.core.product.model.dto.request.PreferentialRateConditionDto;
import dev.crepe.domain.core.product.model.dto.request.RegisterProductRequest;
import dev.crepe.domain.core.product.model.dto.response.RegisterProductResponse;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.model.entity.Tag;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.product.repository.TagRepository;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.infra.s3.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class BankProductServiceImpl implements BankProductService {
    private final BankTokenRepository bankTokenRepository;
    private final AccountRepository accountRepository;
    private final BankRepository bankRepository;
    private final S3Service s3Service;
    private final ProductRepository productRepository;
    private final TagRepository tagRepository;

    @Override
    public RegisterProductResponse registerProduct(String email, MultipartFile productImage, RegisterProductRequest request) {
        Bank bank = bankRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("은행 계정을 찾을 수 없습니다: " + email));

        BankToken bankToken = bankTokenRepository.findByBank(bank)
                .orElseThrow(() -> new EntityNotFoundException("은행의 토큰을 찾을 수 없습니다"));

        Account tokenAccount = accountRepository.findByBankAndBankToken(bank, bankToken)
                .orElseThrow(() ->new EntityNotFoundException(
                        String.format("은행의 %s 토큰 계좌를 찾을 수 없습니다", bankToken.getCurrency())));

        BigDecimal budget = request.getBudget();

        if (tokenAccount.getBalance().compareTo(budget) < 0) {
            throw new InsufficientCapitalException( MessageFormat.format("상품 예산으로 설정할 자금이 부족합니다. 현재 잔액: {0}, 필요 금액: {1}",
                    tokenAccount.getBalance(), budget));
        }

        tokenAccount.deductBalance(budget);
        tokenAccount.addNonAvailableBalance(budget);

        accountRepository.save(tokenAccount);

        String productImageUrl = s3Service.uploadFile(productImage, "product-images");

        EligibilityCriteria eligibilityCriteria = buildEligibilityCriteria(request.getEligibilityCriteria());
        String joinConditionJson = convertEligibilityCriteriaToString(eligibilityCriteria);

        PreferentialRateCondition preferentialRateCondition = buildPreferentialRateCondition(request.getPreferentialRateCondition());

        // 상품 엔티티 생성
        Product product = Product.builder()
                .bank(bank)
                .bankToken(bankToken)
                .productName(request.getProductName())
                .type(request.getType())
                .status(BankProductStatus.WAITING)
                .description(request.getDescription())
                .budget(budget)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .joinCondition(joinConditionJson)
                .baseInterestRate(request.getBaseRate().floatValue())
                .maxMonthlyPayment(request.getMaxMonthlyPayment())
                .maxParticipants(request.getMaxParticipants())
                .imageUrl(productImageUrl)
                .build();

        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            List<Tag> tags = getOrCreateTags(request.getTagNames());
            tags.forEach(product::addTag);
        }


        if (preferentialRateCondition != null) {
            // 각 적용된 우대 금리에 대한 설명 생성
            List<String> rateDescriptions = preferentialRateCondition.getAppliedRateDescriptions();

            // 우대 금리 총합 계산
            BigDecimal totalRate = preferentialRateCondition.calculateTotalRate();

            // 각 우대 금리 설명별 엔티티 생성
            for (String description : rateDescriptions) {
                PreferentialInterestCondition condition = PreferentialInterestCondition.builder()
                        .description(description)
                        .rate(totalRate.floatValue())
                        .build();

                product.addPreferentialCondition(condition);
            }
        }

        Product saved = productRepository.save(product);

        return RegisterProductResponse.builder()
                .productId(saved.getId())
                .productName(saved.getProductName())
                .type(saved.getType()).build();

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
                    // 잘못된 코드 처리
                    log.warn("잘못된 소득 수준 코드: {}", code);
                }
            }
        } else {
            // 선택이 없으면 NO_LIMIT 추가
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

    // 우대 금리 조건 구성 메서드
    private PreferentialRateCondition buildPreferentialRateCondition(PreferentialRateConditionDto selection) {
        if (selection == null) {
            return null;
        }

        AgePreferentialRate ageRate = null;
        if (selection.getAgeRateName() != null && !selection.getAgeRateName().isEmpty()) {
            try {
                ageRate = AgePreferentialRate.valueOf(selection.getAgeRateName());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 연령 우대금리 코드: {}", selection.getAgeRateName());
            }
        }

        DepositPreferentialRate depositRate = null;
        if (selection.getDepositRateName() != null && !selection.getDepositRateName().isEmpty()) {
            try {
                depositRate = DepositPreferentialRate.valueOf(selection.getDepositRateName());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 예치금 우대금리 코드: {}", selection.getDepositRateName());
            }
        }


        FreeDepositCountPreferentialRate freeDepositCountRate = null;
        if (selection.getFreeDepositCountRateName() != null && !selection.getFreeDepositCountRateName().isEmpty()) {
            try {
                freeDepositCountRate = FreeDepositCountPreferentialRate.valueOf(selection.getFreeDepositCountRateName());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 자유납입 우대금리 코드: {}", selection.getFreeDepositCountRateName());
            }
        }

        return PreferentialRateCondition.builder()
                .ageRate(ageRate)
                .depositRate(depositRate)
                .freeDepositCountRate(freeDepositCountRate)
                .build();
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
}
