package dev.crepe.domain.bank.service.impl;

import dev.crepe.domain.core.product.model.BankProductType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Service
public class MaxParticipantsCalculatorService {

    /**
     * 상품 타입별 최대 가입자수 계산
     */
    public Integer calculateMaxParticipants(
            BankProductType productType,
            BigDecimal budget,
            BigDecimal maxMonthlyPayment,
            Float baseInterestRate,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return switch (productType) {
            case VOUCHER -> calculateVoucherMaxParticipants(budget, maxMonthlyPayment);
            case SAVING ->
                    calculateSavingMaxParticipants(budget, maxMonthlyPayment, baseInterestRate, startDate, endDate);
            case INSTALLMENT ->
                    calculateInstallmentMaxParticipants(budget, maxMonthlyPayment, baseInterestRate, startDate, endDate);
            default -> throw new IllegalArgumentException("지원하지 않는 상품 타입입니다: " + productType);
        };
    }

    /**
     * 상품권 최대 가입자수 계산
     * 계산식: budget / maxMonthlyPayment
     */
    private Integer calculateVoucherMaxParticipants(BigDecimal budget, BigDecimal maxMonthlyPayment) {
        if (maxMonthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal result = budget.divide(maxMonthlyPayment, 0, RoundingMode.DOWN);
        return result.intValue();
    }

    /**
     * 예금 최대 가입자수 계산
     * 계산식: budget / (예치금액 * (1 + 연이율 * 예치기간/365))
     */
    private Integer calculateSavingMaxParticipants(
            BigDecimal budget,
            BigDecimal maxMonthlyPayment,
            Float baseInterestRate,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (maxMonthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        // 예치 기간 계산 (일 단위)
        long depositDays = Period.between(startDate, endDate).getDays();
        if (depositDays <= 0) {
            return 0;
        }



        // 연이율을 일이율로 변환
        BigDecimal dailyRate = BigDecimal.valueOf(baseInterestRate / 100.0 / 365.0);

        // 평균 예치 금액 (월 최대 입금액을 기준으로 설정)
        // 만기 시 지급할 총 금액 계산 (원금 + 이자)
        // 총 금액 = 원금 * (1 + 일이율 * 예치일수)
        BigDecimal totalAmountPerPerson = maxMonthlyPayment.multiply(
                BigDecimal.ONE.add(dailyRate.multiply(BigDecimal.valueOf(depositDays)))
        );

        BigDecimal result = budget.divide(totalAmountPerPerson, 0, RoundingMode.DOWN);
        return result.intValue();
    }

    /**
     * 적금 최대 가입자수 계산
     * 복리 적금 공식 사용
     */
    private Integer calculateInstallmentMaxParticipants(
            BigDecimal budget,
            BigDecimal maxMonthlyPayment,
            Float baseInterestRate,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (maxMonthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        // 납입 개월수 계산
        Period period = Period.between(startDate, endDate);
        int months = period.getYears() * 12 + period.getMonths();

        if (months <= 0) {
            return 0;
        }

        // 월이율 계산
        BigDecimal monthlyRate = BigDecimal.valueOf(baseInterestRate / 100.0 / 12.0);

        BigDecimal totalAmountPerPerson;

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            // 이율이 0%인 경우 단순 합계
            totalAmountPerPerson = maxMonthlyPayment.multiply(BigDecimal.valueOf(months));
        } else {
            // 복리 적금 공식: PMT * [((1 + r)^n - 1) / r]
            // PMT: 월 납입액, r: 월이율, n: 납입 개월수

            BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
            BigDecimal onePlusRatePowerN = onePlusRate.pow(months);
            BigDecimal numerator = onePlusRatePowerN.subtract(BigDecimal.ONE);
            BigDecimal futureValueFactor = numerator.divide(monthlyRate, 10, RoundingMode.HALF_UP);

            totalAmountPerPerson = maxMonthlyPayment.multiply(futureValueFactor);
        }

        BigDecimal result = budget.divide(totalAmountPerPerson, 0, RoundingMode.DOWN);
        return result.intValue();
    }

    /**
     * 계산 결과 상세 정보 반환 (디버깅/로깅 용도)
     */
    public MaxParticipantsCalculationResult calculateWithDetails(
            BankProductType productType,
            BigDecimal budget,
            BigDecimal maxMonthlyPayment,
            Float baseInterestRate,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Integer maxParticipants = calculateMaxParticipants(
                productType, budget, maxMonthlyPayment, baseInterestRate, startDate, endDate
        );

        return MaxParticipantsCalculationResult.builder()
                .productType(productType)
                .budget(budget)
                .maxMonthlyPayment(maxMonthlyPayment)
                .baseInterestRate(baseInterestRate)
                .startDate(startDate)
                .endDate(endDate)
                .calculatedMaxParticipants(maxParticipants)
                .build();
    }
    @lombok.Builder
    @lombok.Getter
    public static class MaxParticipantsCalculationResult {
        private BankProductType productType;
        private BigDecimal budget;
        private BigDecimal maxMonthlyPayment;
        private Float baseInterestRate;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer calculatedMaxParticipants;
    }
}
