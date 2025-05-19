package dev.crepe.domain.core.subscribe.model;

import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class PreferentialRateModels {

    @Builder
    @Getter
    public static class InitialRateCalculationResult {
        private float confirmedRate;
        private float potentialRate;
        private List<AppliedCondition> confirmedConditions;
        private List<AppliedCondition> potentialConditions;
        private String appliedRatesJson;
    }

    @Builder
    @Getter
    public static class AppliedCondition {
        private PreferentialInterestCondition condition;
        private ConditionStatus status;
        private float appliedRate;
    }

    @Builder
    @Getter
    public static class ConditionCheckResult {
        private ConditionStatus status;
        private String reason;
    }

    public enum ConditionStatus {
        SATISFIED,
        POTENTIAL,
        NOT_SATISFIED
    }
}
