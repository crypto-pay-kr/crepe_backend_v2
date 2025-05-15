package dev.crepe.domain.core.product.model.dto.eligibility;

import lombok.Getter;

@Getter
public enum AgeGroup {
    YOUTH("청년"),
    MIDDLE_AGED("중장년"),
    SENIOR("노년"),
    ALL_AGES("전연령대");

    private final String description;

    AgeGroup(String description) {
        this.description = description;
    }

    public String getKey() {
        return this.name();
    }
}

