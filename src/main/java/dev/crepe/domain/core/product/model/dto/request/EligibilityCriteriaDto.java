package dev.crepe.domain.core.product.model.dto.request;

import dev.crepe.domain.core.product.model.dto.eligibility.AgeGroup;
import dev.crepe.domain.core.product.model.dto.eligibility.EligibilityCriteria;
import dev.crepe.domain.core.product.model.dto.eligibility.IncomeLevel;
import dev.crepe.domain.core.product.model.dto.eligibility.Occupation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityCriteriaDto {
    @Schema(description = "연령대 조건",
            example = "[\"YOUTH\", \"MIDDLE_AGED\"]",
            allowableValues = {"YOUTH", "MIDDLE_AGED", "SENIOR", "ALL_AGES"})
    private List<String> ageGroups;

    @Schema(description = "직업 조건",
            example = "[\"EMPLOYEE\", \"STUDENT\"]",
            allowableValues = {"ALL_OCCUPATIONS", "EMPLOYEE", "SELF_EMPLOYED", "PUBLIC_SERVANT", "MILITARY", "STUDENT", "HOUSEWIFE", "UNEMPLOYED"})
    private List<String> occupations;

    @Schema(description = "소득 수준 조건",
            example = "[\"NO_LIMIT\"]",
            allowableValues = {"LOW_INCOME", "LIMITED_INCOME", "NO_LIMIT"})
    private List<String> incomeLevels;

    public EligibilityCriteria toEntity() {
        List<AgeGroup> ageGroupEnums = convertStringListToEnumList(ageGroups, AgeGroup.class);
        List<Occupation> occupationEnums = convertStringListToEnumList(occupations, Occupation.class);
        List<IncomeLevel> incomeLevelEnums = convertStringListToEnumList(incomeLevels, IncomeLevel.class);

        return EligibilityCriteria.builder()
                .ageGroups(ageGroupEnums)
                .occupations(occupationEnums)
                .incomeLevels(incomeLevelEnums)
                .build();
    }
    private <T extends Enum<T>> List<T> convertStringListToEnumList(List<String> stringList, Class<T> enumClass) {
        if (stringList == null || stringList.isEmpty()) {
            return new ArrayList<>();
        }

        return stringList.stream()
                .map(name -> {
                    try {
                        return Enum.valueOf(enumClass, name);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid enum name: {} for enum type: {}", name, enumClass.getSimpleName());
                        return null;
                    }
                })
                .filter(enumValue -> enumValue != null)
                .collect(Collectors.toList());
    }
}
