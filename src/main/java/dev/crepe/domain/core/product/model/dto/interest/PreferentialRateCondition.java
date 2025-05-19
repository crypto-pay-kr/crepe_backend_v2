package dev.crepe.domain.core.product.model.dto.interest;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class PreferentialRateCondition {
    private AgePreferentialRate ageRate;
    private DepositPreferentialRate depositRate;
    private FreeDepositCountPreferentialRate freeDepositCountRate;

    /**
     * 총 우대금리를 계산합니다.
     * @return 적용되는 총 우대금리
     */
    public BigDecimal calculateTotalRate() {
        BigDecimal total = BigDecimal.ZERO;

        if (ageRate != null) {
            total = total.add(ageRate.getRate());
        }

        if (depositRate != null) {
            total = total.add(depositRate.getRate());
        }

        if (freeDepositCountRate != null) {
            total = total.add(freeDepositCountRate.getRate());
        }

        return total;
    }

    /**
     * 적용된 우대금리 항목들의 설명을 반환합니다.
     * @return 우대금리 설명 목록
     */
    public List<String> getAppliedRateDescriptions() {
        List<String> descriptions = new ArrayList<>();

        if (ageRate != null && !ageRate.getRate().equals(BigDecimal.ZERO)) {
            descriptions.add(ageRate.getName() + " 우대금리: " + ageRate.getRate() + "% (" + ageRate.getDescription() + ")");
        }

        if (depositRate != null && !depositRate.getRate().equals(BigDecimal.ZERO)) {
            descriptions.add(depositRate.getName() + " 예치금 우대금리: " + depositRate.getRate() + "% (" + depositRate.getDescription() + ")");
        }

        if (freeDepositCountRate != null && !freeDepositCountRate.getRate().equals(BigDecimal.ZERO)) {
            descriptions.add(freeDepositCountRate.getName() + " 자유납입 우대금리: " + freeDepositCountRate.getRate() + "% (" + freeDepositCountRate.getDescription() + ")");
        }

        return descriptions;
    }
}