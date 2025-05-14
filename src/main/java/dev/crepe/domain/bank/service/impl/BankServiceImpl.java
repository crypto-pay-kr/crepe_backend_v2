package dev.crepe.domain.bank.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.bank.service.BankService;
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
import dev.crepe.domain.core.util.coin.regulation.model.entity.Capital;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.repository.CapitalRepository;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.global.repository.PortfolioRepository;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.infra.s3.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {

    private final BankRepository bankRepository;
    private final BankTokenRepository bankTokenRepository;
    private final CapitalRepository capitalRepository;
    private final PortfolioRepository portfolioRepository;
    private final AccountRepository accountRepository;
    private final S3Service s3Service;
    private final ProductRepository productRepository;

    @Override
    public RegisterProductResponse registerProduct(String email, MultipartFile productImage, RegisterProductRequest request) {
        Bank bank = bankRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("은행 계정을 찾을 수 없습니다: " + email));

        Capital capital = capitalRepository.findByBank(bank)
                .orElseThrow(() -> new EntityNotFoundException("은행의 자본금 정보를 찾을 수 없습니다"));

        BankToken bankToken = bankTokenRepository.findByBank(bank)
                .orElseThrow(() ->  new EntityNotFoundException("은행의 토큰을 찾을 수 없습니다"));

        Account tokenAccount = accountRepository.findByBankAndBankToken(bank, bankToken)
                .orElseThrow(() -> new EntityNotFoundException(
                        "은행의 " + bankToken.getCurrency() + " 토큰 계좌를 찾을 수 없습니다"));

        BigDecimal budget = request.getBudget();

        List<Portfolio> portfolios = portfolioRepository.findByBankToken(bankToken);
        if (portfolios.isEmpty()) {
            throw new EntityNotFoundException("은행 토큰의 포트폴리오 정보를 찾을 수 없습니다");
        }

        Map<Coin, BigDecimal> coinAllocationMap = new HashMap<>();
        for (Portfolio portfolio : portfolios) {
            Coin coin = portfolio.getCoin();

            // 해당 코인이 전체 포트폴리오에서 차지하는 비율 계산
            BigDecimal coinRatio = portfolio.getAmount().divide(bankToken.getTotalSupply(), 8, RoundingMode.HALF_UP);

            // 필요한 코인 양 계산
            BigDecimal requiredCoinAmount = budget.multiply(coinRatio);
            coinAllocationMap.put(coin, requiredCoinAmount);

            // 은행의 해당 코인 계좌 조회
            Account coinAccount = accountRepository.findByBankAndCoin(bank, coin)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "은행의 " + coin.getCurrency() + " 계좌를 찾을 수 없습니다"));

            // 자본금 충분성 검증 (가용 잔액으로 확인)
            if (coinAccount.getAvailableBalance().compareTo(requiredCoinAmount) < 0) {
                throw new InsufficientCapitalException(
                        "상품 출시에 필요한 " + coin.getCurrency() + " 자본금이 부족합니다. " +
                                "필요: " + requiredCoinAmount + ", 가용: " + coinAccount.getAvailableBalance());
            }

        }

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

        if (request.getTags() != null) {
            product.addTags(request.getTags());
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

        // 토큰 계좌에서 예산 할당
        tokenAccount.allocateBudget(budget);
        accountRepository.save(tokenAccount);

        RegisterProductResponse response = RegisterProductResponse.builder()
                .productId(saved.getId())
                .productName(saved.getProductName())
                .type(saved.getType()).build();

        return response;

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
                    // 잘못된 코드 처리
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
                    // ALL_OCCUPATIONS가 포함되어 있으면 모든 직업 추가 후 루프 종료
                    if (occupation == Occupation.ALL_OCCUPATIONS) {
                        occupations.clear(); // 기존 목록 비우기
                        occupations.add(Occupation.ALL_OCCUPATIONS);
                        break;
                    }
                    occupations.add(occupation);
                } catch (IllegalArgumentException e) {
                    // 잘못된 코드 처리
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

    // 가입 조건을 JSON 문자열로 변환하는 메서드
    private String convertEligibilityCriteriaToString(EligibilityCriteria criteria) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(criteria);
        } catch (JsonProcessingException e) {
            log.error("가입 조건 직렬화 오류", e);
            return "{}"; // 기본값
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

        RegularDepositPreferentialRate regularDepositRate = null;
        if (selection.getRegularDepositRateName() != null && !selection.getRegularDepositRateName().isEmpty()) {
            try {
                regularDepositRate = RegularDepositPreferentialRate.valueOf(selection.getRegularDepositRateName());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 정기납입 우대금리 코드: {}", selection.getRegularDepositRateName());
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
                .regularDepositRate(regularDepositRate)
                .freeDepositCountRate(freeDepositCountRate)
                .build();
    }
}
