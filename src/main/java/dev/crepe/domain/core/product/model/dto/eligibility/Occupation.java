package dev.crepe.domain.core.product.model.dto.eligibility;

import lombok.Getter;

@Getter
public enum Occupation {
    ALL_OCCUPATIONS("제한 없음"),
    EMPLOYEE("직장인"),
    SELF_EMPLOYED("자영업자"),
    PUBLIC_SERVANT("공무원"),
    MILITARY("군인"),
    STUDENT("학생"),
    HOUSEWIFE("주부"),
    UNEMPLOYED("무직");

    private final String description;

    Occupation(String description) {
        this.description = description;
    }

    public String getKey() {
        return this.name();
    }
}