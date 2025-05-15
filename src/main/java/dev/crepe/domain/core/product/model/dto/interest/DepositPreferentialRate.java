package dev.crepe.domain.core.product.model.dto.interest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
/**
 * 예치금액별 우대금리 enum
 */
@Getter
@RequiredArgsConstructor
public enum DepositPreferentialRate {
    SMALL("소액", "1천만원 미만", BigDecimal.ZERO),
    MEDIUM("중액", "1천만원 이상 5천만원 미만", new BigDecimal("0.2")),
    LARGE("고액", "5천만원 이상 1억원 미만", new BigDecimal("0.3")),
    PREMIUM("프리미엄", "1억원 이상", new BigDecimal("0.5"));

    private final String name;
    private final String description;
    private final BigDecimal rate;  // 우대금리 (%)
}
