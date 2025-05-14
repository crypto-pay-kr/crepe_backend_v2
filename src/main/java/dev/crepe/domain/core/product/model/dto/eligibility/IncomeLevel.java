package dev.crepe.domain.core.product.model.dto.eligibility;

import lombok.Getter;

@Getter
public enum IncomeLevel {
    LOW_INCOME("저소득층"),
    LIMITED_INCOME("소득제한(월 5천 이하)"),
    NO_LIMIT("제한없음");

    private final String description;

    IncomeLevel(String description) {
        this.description = description;
    }

    public String getKey() {
        return this.name();
    }
}