package dev.crepe.domain.core.product.model.dto.interest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 예치금액별 우대금리 enum
 */
@Getter
@RequiredArgsConstructor
public enum DepositPreferentialRate {
    SMALL("소액", "1천만원 미만", BigDecimal.ZERO, new BigDecimal("0"), new BigDecimal("10000000")),
    MEDIUM("중액", "1천만원 이상 5천만원 미만", new BigDecimal("0.2"), new BigDecimal("10000000"), new BigDecimal("50000000")),
    LARGE("고액", "5천만원 이상 1억원 미만", new BigDecimal("0.3"), new BigDecimal("50000000"), new BigDecimal("100000000")),
    PREMIUM("프리미엄", "1억원 이상", new BigDecimal("0.5"), new BigDecimal("100000000"), null);

    private final String name;
    private final String description;
    private final BigDecimal rate;
    private final BigDecimal min;
    private final BigDecimal max;

    public static Optional<DepositPreferentialRate> getTier(BigDecimal amount) {
        for (DepositPreferentialRate tier : values()) {
            boolean minOk = amount.compareTo(tier.min) >= 0;
            boolean maxOk = tier.max == null || amount.compareTo(tier.max) < 0;
            if (minOk && maxOk) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }
}
