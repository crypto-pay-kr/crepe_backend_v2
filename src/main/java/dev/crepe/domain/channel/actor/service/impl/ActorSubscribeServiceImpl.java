package dev.crepe.domain.channel.actor.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.service.ActorSubscribeService;
import dev.crepe.domain.channel.actor.user.exception.UserNotFoundException;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.product.model.dto.eligibility.AgeGroup;
import dev.crepe.domain.core.product.model.dto.eligibility.EligibilityCriteria;
import dev.crepe.domain.core.product.model.dto.eligibility.IncomeLevel;
import dev.crepe.domain.core.product.model.dto.eligibility.Occupation;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.subscribe.model.PotentialType;
import dev.crepe.domain.core.subscribe.model.PreferentialRateModels;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.dto.request.SubscribeProductRequest;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeProductResponse;
import dev.crepe.domain.core.subscribe.model.entity.PotentialPreferentialCondition;
import dev.crepe.domain.core.subscribe.model.entity.PreferentialConditionSatisfaction;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.PotentialPreferentialConditionRepository;
import dev.crepe.domain.core.subscribe.repository.PreferentialConditionSatisfactionRepository;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.subscribe.service.PreferentialConditionSatisfactionService;
import dev.crepe.domain.core.subscribe.util.PreferentialRateUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActorSubscribeServiceImpl implements ActorSubscribeService {
    private final ActorRepository actorRepository;
    private final ProductRepository productRepository;
    private final SubscribeRepository subscribeRepository;
    private final ObjectMapper objectMapper;
    private final PreferentialRateUtils preferentialRateUtils;
    private final PotentialPreferentialConditionRepository potentialPreferentialConditionRepository;
    private final PreferentialConditionSatisfactionService satisfactionService;

    // 상품 구독
    @Override
    @Transactional
    public SubscribeProductResponse subscribeProduct(String userEmail, SubscribeProductRequest request) {
        Actor user = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // 이미 가입한 상품인지 확인
        if (subscribeRepository.existsByUserAndProduct(user, product)) {
            throw new IllegalStateException("이미 가입한 상품입니다.");
        }

        checkEligibility(user, product);
        PreferentialRateModels.InitialRateCalculationResult initialRates = calculateInitialPreferentialRates(user, product, request);

        // 3. 구독 엔티티 생성
        LocalDateTime now = LocalDateTime.now();
        Subscribe.SubscribeBuilder subscribeBuilder = Subscribe.builder()
                .user(user)
                .product(product)
                .status(SubscribeStatus.ACTIVE)
                .subscribeDate(now)
                .expiredDate(now.plusYears(1))
                .balance(request.getInitialDepositAmount() != null ? request.getInitialDepositAmount() : BigDecimal.ZERO)
                .interestRate(initialRates.getConfirmedRate())
                .appliedPreferentialRates(initialRates.getAppliedRatesJson());

        // 4. 상품 타입별 추가 설정
        if (product.getType() == BankProductType.INSTALLMENT) {
            subscribeBuilder
                    .currentMonthDepositCount(0)
                    .selectedFreeDepositRate(request.getSelectedFreeDepositRate()); // 목표만 설정
        } else if (product.getType() == BankProductType.VOUCHER) {
            subscribeBuilder
                    .interestRate(0)
                    .voucherCode(generateVoucherCode());
        }

        Subscribe saved = subscribeRepository.save(subscribeBuilder.build());

        // 5. 가입 시점 우대금리 조건 만족도 기록
        recordInitialConditionSatisfactions(user, saved, product, initialRates);

        return buildSubscribeResponse(saved, product, initialRates);
    }



    private boolean checkAgeEligibility(Actor user, EligibilityCriteria criteria) {
        // 모든 연령 허용이면 통과
        if (criteria.isAllAges()) {
            return true;
        }
        // 사용자 주민번호 앞자리로 연령대 결정
        String birthDate = user.getBirth();
        AgeGroup userAgeGroup = preferentialRateUtils.determineAgeGroup(birthDate);

        if (userAgeGroup == null) {
            return false; // 생년월일 정보가 유효하지 않으면 실패
        }

        // 해당 연령대가 조건에 포함되는지 확인
        return criteria.getAgeGroups().contains(userAgeGroup);
    }

    /**
     * 직업 조건 확인
     */
    private boolean checkOccupationEligibility(Actor user, EligibilityCriteria criteria) {
        // 모든 직업 허용이면 통과
        if (criteria.getOccupations().contains(Occupation.ALL_OCCUPATIONS)) {
            return true;
        }

        // 사용자 직업 확인
        Occupation userOccupation = user.getOccupation();

        return criteria.getOccupations().contains(userOccupation);
    }

    /**
     * 소득 수준 조건 확인
     */
    private boolean checkIncomeEligibility(Actor user, EligibilityCriteria criteria) {
        // 소득 제한 없음이면 통과
        if (criteria.hasNoIncomeLimit()) {
            return true;
        }

        BigDecimal annualIncome = user.getAnnualIncome();

        for (IncomeLevel incomeLevel : criteria.getIncomeLevels()) {
            if (incomeLevel == IncomeLevel.LOW_INCOME && annualIncome.compareTo(new BigDecimal("30000000")) <= 0) {
                // 연 소득 3000만원 이하는 저소득층
                return true;
            } else if (incomeLevel == IncomeLevel.LIMITED_INCOME &&
                    annualIncome.compareTo(new BigDecimal("60000000")) <= 0) {
                // 연 소득 6000만원 이하는 소득제한(월 5천 이하)
                return true;
            } else if (incomeLevel == IncomeLevel.NO_LIMIT) {
                // 제한 없음은 모든 소득 수준 허용
                return true;
            }
        }

        return false;
    }

    /**
     * 사용자가 상품 가입 조건에 부합하는지 확인
     */
    public void checkEligibility(Actor user, Product product) {
        // 상품의 가입 조건 파싱
        EligibilityCriteria eligibilityCriteria;
        try {
            eligibilityCriteria = objectMapper.readValue(product.getJoinCondition(), EligibilityCriteria.class);
        } catch (IOException e) {
            throw new IllegalStateException("상품 가입 조건을 확인할 수 없습니다.");
        }

        // 연령 확인
        if (!checkAgeEligibility(user, eligibilityCriteria)) {
            throw new IllegalStateException("연령 조건에 부합하지 않습니다.");
        }

        // 직업 확인
        if (!checkOccupationEligibility(user, eligibilityCriteria)) {
            throw new IllegalStateException("직업 조건에 부합하지 않습니다.");
        }

        // 소득 수준 확인
        if (!checkIncomeEligibility(user, eligibilityCriteria)) {
            throw new IllegalStateException("소득 수준 조건에 부합하지 않습니다.");
        }
    }

    /**
     * 상품권 코드 생성(UUID를 사용하여 고유 코드 생성)
     */
    private String generateVoucherCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }



    private PreferentialRateModels.InitialRateCalculationResult calculateInitialPreferentialRates(Actor user, Product product, SubscribeProductRequest request) {
        float baseRate = product.getBaseInterestRate();
        float confirmedRate = baseRate; // 확정된 금리
        float potentialRate = baseRate; // 잠재적 금리 (자유납입 포함)

        List<PreferentialRateModels.AppliedCondition> confirmedConditions = new ArrayList<>(); // 확정된 조건들
        List<PreferentialRateModels.AppliedCondition> potentialConditions = new ArrayList<>(); // 잠재적 조건들

        for (PreferentialInterestCondition condition : product.getPreferentialConditions()) {
            // 상품 타입을 전달하여 조건 체크
            PreferentialRateModels.ConditionCheckResult result = checkConditionAtSubscription(user, condition, request, product.getType());

            switch (result.getStatus()) {
                case SATISFIED:
                    // 즉시 적용 가능
                    confirmedRate += condition.getRate();
                    potentialRate += condition.getRate();
                    confirmedConditions.add(PreferentialRateModels.AppliedCondition.builder()
                            .condition(condition)
                            .status(PreferentialRateModels.ConditionStatus.SATISFIED)
                            .appliedRate(condition.getRate())
                            .build());
                    break;

                case POTENTIAL:
                    // 나중에 조건 충족 시 적용 가능
                    potentialRate += condition.getRate();
                    potentialConditions.add(PreferentialRateModels.AppliedCondition.builder()
                            .condition(condition)
                            .status(PreferentialRateModels.ConditionStatus.POTENTIAL)
                            .appliedRate(condition.getRate())
                            .build());
                    break;

                case NOT_SATISFIED:
                    // 적용 불가
                    break;
            }
        }

        return PreferentialRateModels.InitialRateCalculationResult.builder()
                .confirmedRate(confirmedRate)
                .potentialRate(potentialRate)
                .confirmedConditions(confirmedConditions)
                .potentialConditions(potentialConditions)
                .appliedRatesJson(preferentialRateUtils.buildAppliedRatesJson(confirmedConditions, potentialConditions))
                .build();
    }

    // 가입 시점에 조건 체크
    private PreferentialRateModels.ConditionCheckResult checkConditionAtSubscription(Actor user, PreferentialInterestCondition condition, SubscribeProductRequest request, BankProductType productType) {
        String title = condition.getTitle().toLowerCase();

        // 1. 연령 조건 - 모든 상품에서 즉시 확정 가능
        if (title.contains("청년") || title.contains("중장년") || title.contains("노년")) {
            boolean satisfied = preferentialRateUtils.checkAgeCondition(user, condition);
            return PreferentialRateModels.ConditionCheckResult.builder()
                    .status(satisfied ? PreferentialRateModels.ConditionStatus.SATISFIED : PreferentialRateModels.ConditionStatus.NOT_SATISFIED)
                    .reason(satisfied ? "연령 조건 만족" : "연령 조건 불만족")
                    .build();
        }

        // 2. 예치금액 조건
        if (title.contains("소액") || title.contains("중액") || title.contains("고액") || title.contains("프리미엄")) {
            if (productType == BankProductType.SAVING) {
                // 예금: 초기 예치금액으로 즉시 확정
                BigDecimal initialAmount = request.getInitialDepositAmount() != null ?
                        request.getInitialDepositAmount() : BigDecimal.ZERO;
                boolean satisfied = preferentialRateUtils.checkDepositAmountCondition(initialAmount, condition);
                return PreferentialRateModels.ConditionCheckResult.builder()
                        .status(satisfied ? PreferentialRateModels.ConditionStatus.SATISFIED : PreferentialRateModels.ConditionStatus.NOT_SATISFIED)
                        .reason(satisfied ? "예치금액 조건 만족" : "예치금액 조건 불만족")
                        .build();
            } else if (productType == BankProductType.INSTALLMENT) {
                // 적금: 누적 예치금액에 따라 나중에 결정 (잠재적)
                return PreferentialRateModels.ConditionCheckResult.builder()
                        .status(PreferentialRateModels.ConditionStatus.POTENTIAL)
                        .reason("누적 예치금액에 따라 추후 적용")
                        .build();
            }
        }

        // 3. 자유납입 조건 - 적금에만 해당 (잠재적)
        if (title.contains("자유납입") || title.contains("초급") || title.contains("중급") || title.contains("고급")) {
            if (productType == BankProductType.INSTALLMENT) {
                if (request.getSelectedFreeDepositRate() != null) {
                    boolean matches = preferentialRateUtils.checkFreeDepositSelection(request.getSelectedFreeDepositRate(), condition);
                    return PreferentialRateModels.ConditionCheckResult.builder()
                            .status(matches ? PreferentialRateModels.ConditionStatus.POTENTIAL : PreferentialRateModels.ConditionStatus.NOT_SATISFIED)
                            .reason(matches ? "자유납입 목표 설정됨" : "자유납입 목표 불일치")
                            .build();
                } else {
                    return PreferentialRateModels.ConditionCheckResult.builder()
                            .status(PreferentialRateModels.ConditionStatus.NOT_SATISFIED)
                            .reason("자유납입 목표 미설정")
                            .build();
                }
            } else {
                // 예금이나 상품권에는 자유납입 조건이 없음
                return PreferentialRateModels.ConditionCheckResult.builder()
                        .status(PreferentialRateModels.ConditionStatus.NOT_SATISFIED)
                        .reason("해당 상품에는 자유납입 조건이 적용되지 않음")
                        .build();
            }
        }

        return PreferentialRateModels.ConditionCheckResult.builder()
                .status(PreferentialRateModels.ConditionStatus.NOT_SATISFIED)
                .reason("알 수 없는 조건")
                .build();
    }

    // 가입 시점 우대금리 조건 만족도 기록

    private void recordInitialConditionSatisfactions(Actor user, Subscribe subscribe, Product product,
                                                     PreferentialRateModels.InitialRateCalculationResult result) {
        // 즉시 충족된 조건들만 기록
        for (PreferentialRateModels.AppliedCondition confirmedCondition : result.getConfirmedConditions()) {
            PreferentialInterestCondition condition = confirmedCondition.getCondition();
            satisfactionService.recordImmediateSatisfaction(user, subscribe, condition);
        }

        // 잠재적 조건들은 PotentialPreferentialCondition에만 기록 (기존과 동일)
        if (product.getType() == BankProductType.INSTALLMENT && !result.getPotentialConditions().isEmpty()) {
            recordPotentialConditions(user, subscribe, product, result);
        }
    }

    // 응답 생성
    private SubscribeProductResponse buildSubscribeResponse(Subscribe saved, Product product,
                                                            PreferentialRateModels.InitialRateCalculationResult result) {
        SubscribeProductResponse.SubscribeProductResponseBuilder builder = SubscribeProductResponse.builder()
                .productName(product.getProductName())
                .productType(product.getType())
                .status(saved.getStatus())
                .subscribeDate(saved.getSubscribeDate())
                .expiredDate(saved.getExpiredDate())
                .balance(saved.getBalance())
                .interestRate(saved.getInterestRate()) // 현재 확정된 금리
                .message("상품 가입이 완료되었습니다.");

        // 잠재적 우대금리가 있는 경우 안내 - 적금에만 해당
        if (product.getType() == BankProductType.INSTALLMENT && result.getPotentialRate() > result.getConfirmedRate()) {
            float additionalRate = result.getPotentialRate() - result.getConfirmedRate();

            // 잠재적 조건의 유형에 따라 다른 메시지
            List<String> potentialMessages = new ArrayList<>();
            for (PreferentialRateModels.AppliedCondition condition : result.getPotentialConditions()) {
                String title = condition.getCondition().getTitle().toLowerCase();
                if (title.contains("자유납입") || title.contains("초급") || title.contains("중급") || title.contains("고급")) {
                    potentialMessages.add("자유납입 조건");
                } else if (title.contains("소액") || title.contains("중액") || title.contains("고액") || title.contains("프리미엄")) {
                    potentialMessages.add("예치금액 조건");
                }
            }

            String conditionText = potentialMessages.isEmpty() ? "조건" : String.join(", ", potentialMessages);
            builder.additionalMessage(String.format("%s 충족 시 추가로 +%.2f%% 혜택을 받을 수 있습니다.", conditionText, additionalRate));
        }

        if (product.getType() == BankProductType.VOUCHER) {
            builder.voucherCode(saved.getVoucherCode());
        }

        return builder.build();
    }

    private void recordPotentialConditions(Actor user, Subscribe subscribe, Product product,
                                           PreferentialRateModels.InitialRateCalculationResult result) {
        for (PreferentialRateModels.AppliedCondition potentialCondition : result.getPotentialConditions()) {
            PreferentialInterestCondition condition = potentialCondition.getCondition();

            // 조건 유형 판별
            PotentialType type = determinePotentialType(condition);
            String targetValue = determineTargetValue(condition, subscribe);

            PotentialPreferentialCondition potential = PotentialPreferentialCondition.create(
                    user, subscribe, condition, type, targetValue);

            potentialPreferentialConditionRepository.save(potential);
        }
    }

    // 조건 유형 및 목표값 결정
    private PotentialType determinePotentialType(PreferentialInterestCondition condition) {
        String title = condition.getTitle().toLowerCase();

        if (title.contains("자유납입") || title.contains("납입횟수") ||
                title.contains("초급") || title.contains("중급") || title.contains("고급")) {
            return PotentialType.FREE_DEPOSIT_COUNT;
        } else if (title.contains("소액") || title.contains("중액") || title.contains("고액") || title.contains("프리미엄")) {
            return PotentialType.ACCUMULATE_DEPOSIT;
        } else if (title.contains("누적") || title.contains("추가예치")) {
            return PotentialType.ACCUMULATE_DEPOSIT;
        } else if (title.contains("지속성")) {
            return PotentialType.MONTHLY_DEPOSIT_FREQUENCY;
        }

        return PotentialType.FREE_DEPOSIT_COUNT; // 기본값
    }

    private String determineTargetValue(PreferentialInterestCondition condition, Subscribe subscribe) {
        String title = condition.getTitle().toLowerCase();

        if (title.contains("초급") || title.contains("3회")) {
            return "3";
        } else if (title.contains("중급") || title.contains("5회")) {
            return "5";
        } else if (title.contains("고급") || title.contains("10회")) {
            return "10";
        }

        // 자유납입의 경우 Subscribe의 selectedFreeDepositRate에서 추출
        if (subscribe.getSelectedFreeDepositRate() != null) {
            return switch (subscribe.getSelectedFreeDepositRate()) {
                case LEVEL1 -> "3";
                case LEVEL2 -> "5";
                case LEVEL3 -> "10";
                default -> "0";
            };
        }

        return "0";
    }


}
