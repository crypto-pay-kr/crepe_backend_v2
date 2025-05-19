package dev.crepe.domain.core.product.model.dto.interest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * 연령대별 우대금리 enum
 */
@Getter
@RequiredArgsConstructor
public enum AgePreferentialRate {
    YOUTH("청년", "만 19세 이상 34세 이하", new BigDecimal("0.3")),
    MIDDLE_AGED("중장년", "만 35세 이상 64세 이하", new BigDecimal("0.2")),
    SENIOR("노년층", "만 65세 이상", new BigDecimal("0.5")),
    ALL_AGES("전연령", "나이 제한 없음", BigDecimal.ZERO);  // 전연령은 추가 우대금리 없음

    private final String name;
    private final String description;
    private final BigDecimal rate;
}
