package dev.crepe.domain.core.subscribe.model;

public enum PotentialType {
    FREE_DEPOSIT_COUNT("자유납입 횟수"),
    ACCUMULATE_DEPOSIT("누적 예치금액"),
    REGULAR_DEPOSIT_COMPLIANCE("정기납입 준수");

    private final String description;

    PotentialType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
