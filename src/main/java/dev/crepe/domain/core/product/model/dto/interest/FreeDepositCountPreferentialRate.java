package dev.crepe.domain.core.product.model.dto.interest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * 자유 납입 횟수 달성 우대금리 enum
 */
@Getter
@RequiredArgsConstructor
public enum FreeDepositCountPreferentialRate {
    NONE("없음", "자유 납입 없음", BigDecimal.ZERO),
    LEVEL1("초급", "월 3회 이상 자유 납입", new BigDecimal("0.1")),
    LEVEL2("중급", "월 5회 이상 자유 납입", new BigDecimal("0.2")),
    LEVEL3("고급", "월 10회 이상 자유 납입", new BigDecimal("0.3"));

    private final String name;
    private final String description;
    private final BigDecimal rate;  // 우대금리 (%)

    public static FreeDepositCountPreferentialRate matchByDepositCount(int count) {
        if (count >= 10) return LEVEL3;
        if (count >= 5) return LEVEL2;
        if (count >= 3) return LEVEL1;
        return NONE;
    }
}