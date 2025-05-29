package dev.crepe.domain.core.subscribe.scheduler.expired.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.product.model.dto.interest.DepositPreferentialRate;
import dev.crepe.domain.core.product.model.dto.interest.FreeDepositCountPreferentialRate;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.subscribe.exception.*;
import dev.crepe.domain.core.subscribe.model.PotentialType;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.dto.response.PreferentialRateInfo;
import dev.crepe.domain.core.subscribe.model.entity.PotentialPreferentialCondition;
import dev.crepe.domain.core.subscribe.model.entity.PreferentialConditionSatisfaction;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.PotentialPreferentialConditionRepository;
import dev.crepe.domain.core.subscribe.repository.PreferentialConditionRepository;
import dev.crepe.domain.core.subscribe.repository.PreferentialConditionSatisfactionRepository;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.subscribe.scheduler.expired.service.SubscribeExpiredService;
import dev.crepe.domain.core.util.history.subscribe.model.SubscribeHistoryType;
import dev.crepe.domain.core.util.history.subscribe.repository.SubscribeHistoryRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SubscribeExpiredServiceImpl implements SubscribeExpiredService {

    private final AccountRepository accountRepository;
    private final PreferentialConditionSatisfactionRepository preferentialConditionSatisfactionRepository;
    private final PreferentialConditionRepository preferentialConditionRepository;
    private final SubscribeRepository subscribeRepository;
    private final SubscribeHistoryRepository subscribeHistoryRepository;
    private final PotentialPreferentialConditionRepository potentialPreferentialConditionRepository;
    private final ExceptionDbService exceptionDbService;


    @Transactional
    public String expired(String userEmail, Long subscribeId) {
        // 가입 정보 조회
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(() -> {
                    exceptionDbService.throwException("SUBSCRIBE_04");
                    return null;
                });

        if (subscribe.getStatus() == SubscribeStatus.EXPIRED) {
            exceptionDbService.throwException("SUBSCRIBE_01");
        }


        Product product = subscribe.getProduct();
        BigDecimal balance = subscribe.getBalance();


        // 예치금 확인
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            exceptionDbService.throwException("SUBSCRIBE_02");
        }

        // 만기일 도달 여부 확인
        if (!subscribe.isMatured()) {
            exceptionDbService.throwException("SUBSCRIBE_03");
        };


        // 기본 금리
        BigDecimal baseInterestRate = new BigDecimal(Float.toString(product.getBaseInterestRate()));
        BigDecimal baseRate = baseInterestRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.DOWN); // 소수점자리로 변환
        BigDecimal preTaxInterest;


        switch (product.getType()) {
            case SAVING -> {
                preTaxInterest = balance.multiply(baseRate);
            }
            case INSTALLMENT -> {
                preTaxInterest = calculateInstallmentCompoundInterest(subscribe, baseRate);
            }
            default -> throw new UnsupportedProductTypeException();
        }

        // 우대금리 조건 평가 및 저장
        evaluateConfirmedPreferentialConditions(subscribe, subscribe.getUser());

        // 우대금리 조건 충족 내역 조회
        List<PreferentialConditionSatisfaction> satisfiedConditions =
                preferentialConditionSatisfactionRepository.findBySubscribe_IdAndIsSatisfiedTrue(subscribe.getId());

        // 우대 금리 총합 계산
        BigDecimal additionalRate = satisfiedConditions.stream()
                .map(cond -> BigDecimal.valueOf(cond.getCondition().getRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);



        BigDecimal preferentialRate = additionalRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.DOWN);

        // 우대 금리 이자
        BigDecimal preferentialInterest = (balance.add(preTaxInterest)).multiply(preferentialRate).setScale(10, RoundingMode.DOWN);
        BigDecimal totalInterest = preTaxInterest.add(preferentialInterest);


        // 세후 이자 계산
        BigDecimal taxRate = BigDecimal.valueOf(0.154);
        BigDecimal postTaxInterest = totalInterest.multiply(BigDecimal.ONE.subtract(taxRate));



        // 사용자 토큰 계좌에 원금 + 세후 이자 지급
        Account userTokenAccount = accountRepository.findByActor_EmailAndBankTokenId(
                subscribe.getUser().getEmail(), product.getBankToken().getId()
        ).orElseThrow(UserAccountNotFoundException::new);


        // 은행 자본금 계좌에서 이자 차감
        Account bankTokenAccount = accountRepository
                .findByBankTokenIdAndActorIsNull(product.getBankToken().getId())
                .orElseThrow(BankAccountNotFoundException::new);

        bankTokenAccount.reduceNonAvailableBalance(postTaxInterest);


        BigDecimal totalPayout = balance.add(postTaxInterest);
        userTokenAccount.addAmount(totalPayout);


        // 상태 변경 (만기 처리)
        subscribe.changeExpired();
        subscribeRepository.save(subscribe);

        return "만기 완료";
    }

    // 적금 복리 이자 계산
    private BigDecimal calculateInstallmentCompoundInterest(Subscribe subscribe, BigDecimal annualRate) {
        BigDecimal totalBalance = BigDecimal.ZERO; // 총 입금액 (원금)
        BigDecimal totalAmount = BigDecimal.ZERO;  // 누적된 원리금

        LocalDate startDate = subscribe.getSubscribeDate().toLocalDate();
        LocalDate endDate = subscribe.getExpiredDate().toLocalDate();

        int totalMonths = (int) ChronoUnit.MONTHS.between(startDate, endDate) ;

        // 월복리 이율 = (1 + 연이율)^(1/12) - 1
        BigDecimal monthlyRate = BigDecimal.valueOf(
                Math.pow(annualRate.add(BigDecimal.ONE).doubleValue(), 1.0 / 12.0) - 1
        ).setScale(10, RoundingMode.DOWN);


        for (int i = 0; i < totalMonths; i++) {
            LocalDate monthDate = startDate.plusMonths(i);
            LocalDate monthEnd = monthDate.withDayOfMonth(monthDate.lengthOfMonth()); // 매 월 말일

            LocalDateTime startOfMonth = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = monthEnd.atTime(23, 59, 59);

            // 월별 입금합 조회
            BigDecimal deposited = subscribeHistoryRepository.sumMonthlyDeposit(
                    subscribe,
                    SubscribeHistoryType.DEPOSIT,
                    startOfMonth,
                    endOfMonth
            );


            if (deposited != null && deposited.compareTo(BigDecimal.ZERO) > 0) {
                totalAmount = totalAmount.add(deposited);
                totalBalance = totalBalance.add(deposited);
            }

            // 복리 이자 적용
            totalAmount = totalAmount.multiply(BigDecimal.ONE.add(monthlyRate))
                    .setScale(10, RoundingMode.DOWN);


        }

        // 일할 이자 계산
        if (!endDate.equals(endDate.withDayOfMonth(endDate.lengthOfMonth()))) {
            int daysPast = endDate.getDayOfMonth() - 1;
            int daysInMonth = endDate.lengthOfMonth();

            BigDecimal thisMonthInterest = totalAmount.multiply(monthlyRate).setScale(10, RoundingMode.DOWN);
            BigDecimal daysInterest = thisMonthInterest.multiply(
                    BigDecimal.valueOf(daysPast)
                            .divide(BigDecimal.valueOf(daysInMonth), 10, RoundingMode.DOWN)
            ).setScale(10, RoundingMode.DOWN);

            totalAmount = totalAmount.add(daysInterest);
        }

        // 총 이자 = 누적금 - 원금
        BigDecimal totalInterest = totalAmount.subtract(totalBalance).setScale(10, RoundingMode.DOWN);

        return totalInterest;

    }



    // 적금 단리 이자 계산
    private BigDecimal calculateInstallmentSimpleInterest(Subscribe subscribe, BigDecimal annualRate) {
        BigDecimal totalInterest = BigDecimal.ZERO;
        boolean hasMissingDepositMonth = false;

        LocalDate startDate = subscribe.getSubscribeDate().toLocalDate(); // 가입일
        LocalDate endDate = subscribe.getExpiredDate().toLocalDate();     // 만기일
        int totalMonths = (int)ChronoUnit.MONTHS.between(startDate, endDate);

        for (int i = 0; i < totalMonths; i++) {
            LocalDate monthStart = startDate.plusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            LocalDateTime startOfMonth = monthStart.atStartOfDay();
            LocalDateTime endOfMonth = monthEnd.atTime(23, 59, 59);

            // 월별 입금합 조회
            BigDecimal deposited = subscribeHistoryRepository.sumMonthlyDeposit(
                    subscribe,
                    SubscribeHistoryType.DEPOSIT,
                    startOfMonth,
                    endOfMonth
            );

            // 입금 없는 달은 건너뜀
            if (deposited == null || deposited.compareTo(BigDecimal.ZERO) == 0) {
                hasMissingDepositMonth = true;
                continue;
            }

            int remainingMonths = totalMonths- i;

            // 월별 이자 = 입금액 × 연이율 × (잔여개월 / 12)
            BigDecimal interest = deposited
                    .multiply(annualRate)
                    .multiply(BigDecimal.valueOf(remainingMonths))
                    .divide(BigDecimal.valueOf(12), 10, RoundingMode.DOWN);

            totalInterest = totalInterest.add(interest);
        }

        // 입금 없는 달이 있을 경우 예외 처리
        if (hasMissingDepositMonth) {
            throw new IllegalStateException("만기 해지를 위해 월에 최소 1회 이상 입금이 필요합니다.");
        }

        return totalInterest;
    }

    // 월 입금 횟수 계산
    private FreeDepositCountPreferentialRate calculateFreeDepositRate(Subscribe subscribe) {
        LocalDate startDate = subscribe.getSubscribeDate().toLocalDate();
        LocalDate endDate = subscribe.getExpiredDate().toLocalDate();

        int totalMonths = (int) ChronoUnit.MONTHS.between(startDate, endDate);

        int minMonthlyDepositCount = Integer.MAX_VALUE;

        for (int i = 0; i < totalMonths; i++) {
            LocalDate monthStart = startDate.plusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            LocalDateTime startOfMonth = monthStart.atStartOfDay();
            LocalDateTime endOfMonth = monthEnd.atTime(23, 59, 59);

            // 월별 자유 납입 횟수
            int depositCount = subscribeHistoryRepository.countMonthlyDepositEvents(
                    subscribe,
                    SubscribeHistoryType.DEPOSIT,
                    startOfMonth,
                    endOfMonth
            );

            // 가장 낮은 횟수 저장
            minMonthlyDepositCount = Math.min(minMonthlyDepositCount, depositCount);
        }

        // 가장 낮은 월 납입 횟수를 기준으로 우대금리 등급 결정
        if (minMonthlyDepositCount >= 10) return FreeDepositCountPreferentialRate.LEVEL3;
        if (minMonthlyDepositCount >= 5) return FreeDepositCountPreferentialRate.LEVEL2;
        if (minMonthlyDepositCount >= 3) return FreeDepositCountPreferentialRate.LEVEL1;
        return FreeDepositCountPreferentialRate.NONE;
    }


    public void evaluateConfirmedPreferentialConditions(Subscribe subscribe, Actor user)  {
        List<PreferentialRateInfo> confirmed = new ArrayList<>();

        for (PotentialPreferentialCondition potential : potentialPreferentialConditionRepository.findBySubscribe(subscribe)) {
            PreferentialInterestCondition condition = potential.getCondition();
            PotentialType type = potential.getPotentialType();

            // 예치금 기준 우대 조건 확인 (예/적금)
            switch (type) {
                case ACCUMULATE_DEPOSIT -> {
                    DepositPreferentialRate tier = DepositPreferentialRate.getTier(subscribe.getBalance()).orElse(null);
                    if (tier != null && tier.getName().equals(condition.getTitle())) {
                        confirmed.add(new PreferentialRateInfo(
                                tier.getRate(), tier.getDescription(), tier.getName(), "CONFIRMED"
                        ));
                    }
                    System.out.println("📌 비교 title: " + tier.getName() + " vs " + condition.getTitle());

                }


                // 자유 납입 횟수 기준 우대 조건 확인 (적금)
                case FREE_DEPOSIT_COUNT -> {
                    if (subscribe.getProduct().getType() == BankProductType.INSTALLMENT) {
                        FreeDepositCountPreferentialRate tier = calculateFreeDepositRate(subscribe);
                        if (tier != null && tier.getName().equals(condition.getTitle())) {
                            confirmed.add(new PreferentialRateInfo(
                                    tier.getRate(), tier.getDescription(), tier.getName(), "CONFIRMED"
                            ));
                        }
                        System.out.println("📌 비교 title: " + tier.getName() + " vs " + condition.getTitle());
                    }
                }

            }
        }

        // PreferentialRates 컬럼에 저장
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(Map.of(
                    "confirmedCount", confirmed.size(),
                    "confirmed", confirmed
            ));
            subscribe.setAppliedPreferentialRates(json);
        } catch (JsonProcessingException e) {
            subscribe.setAppliedPreferentialRates("{}");
        }


        // 확정 조건 저장
        for (PreferentialRateInfo info : confirmed) {
            Optional<PreferentialInterestCondition> optional = preferentialConditionRepository.findByProductAndTitle(subscribe.getProduct(),info.getTitle());

            // 조건이 없을 경우
            if (optional.isEmpty()) {
                continue;
            }

            PreferentialInterestCondition condition = optional.get();
            PreferentialConditionSatisfaction record = PreferentialConditionSatisfaction.createImmediateSatisfaction(
                    user, subscribe, condition
            );
            preferentialConditionSatisfactionRepository.save(record);
        }


    }


}

