package dev.crepe.domain.core.subscribe.model;

public enum PotentialType {
    FREE_DEPOSIT_COUNT("자유납입 횟수"),
    ACCUMULATE_DEPOSIT("누적 예치금액"),
    MONTHLY_DEPOSIT_FREQUENCY("월별 납입 지속성");

    private final String description;

    PotentialType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
