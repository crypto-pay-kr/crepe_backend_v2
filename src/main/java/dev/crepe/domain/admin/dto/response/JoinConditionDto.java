package dev.crepe.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinConditionDto {
    private List<String> ageGroups;      // ["YOUTH", "MIDDLE_AGED"]
    private List<String> occupations;    // ["ALL_OCCUPATIONS"] 또는 ["STUDENT", "WORKER"]
    private List<String> incomeLevels;   // ["NO_LIMIT"] 또는 ["LOW_INCOME", "LIMITED_INCOME"]
    private boolean allAges;             // 모든 연령 허용 여부
}