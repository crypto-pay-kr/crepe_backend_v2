package dev.crepe.domain.core.product.model.dto.interest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * 정기 납입 우대금리 enum
 */
@Getter
@RequiredArgsConstructor
public enum RegularDepositPreferentialRate {
    NONE("없음", "정기 납입 없음", BigDecimal.ZERO),
    MONTHLY("월 1회", "매월 약정일에 납입", new BigDecimal("0.3")),
    FORTNIGHTLY("2주 1회", "2주마다 약정일에 납입", new BigDecimal("0.4")),
    WEEKLY("주 1회", "매주 약정일에 납입", new BigDecimal("0.5"));

    private final String name;
    private final String description;
    private final BigDecimal rate;  // 우대금리 (%)
}