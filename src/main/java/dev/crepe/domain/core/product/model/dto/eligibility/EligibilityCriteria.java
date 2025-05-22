package dev.crepe.domain.core.product.model.dto.eligibility;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EligibilityCriteria {
    private List<AgeGroup> ageGroups;
    private List<Occupation> occupations;
    private List<IncomeLevel> incomeLevels;

    // 전체 대상인지 확인하는 유틸리티 메서드
    public boolean isAllAges() {
        return ageGroups.contains(AgeGroup.ALL_AGES);
    }

    public boolean hasNoIncomeLimit() {
        return incomeLevels.contains(IncomeLevel.NO_LIMIT);
    }

    // 모든 대상을 포함하는 객체 생성
    public static EligibilityCriteria createForAll() {
        return EligibilityCriteria.builder()
                .ageGroups(List.of(AgeGroup.ALL_AGES))
                .occupations(List.of(Occupation.ALL_OCCUPATIONS))
                .incomeLevels(List.of(IncomeLevel.NO_LIMIT))
                .build();
    }
}

