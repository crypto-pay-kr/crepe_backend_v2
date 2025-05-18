package dev.crepe.domain.core.product.model.dto.request;

import dev.crepe.domain.core.product.model.dto.interest.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferentialRateConditionDto {
    @Schema(description = "연령대별 우대금리",
            example = "YOUTH",
            allowableValues = {"YOUTH", "MIDDLE_AGED", "SENIOR", "ALL_AGES"})
    private String ageRateName;

    @Schema(description = "예치금액별 우대금리",
            example = "MEDIUM",
            allowableValues = {"SMALL", "MEDIUM", "LARGE", "PREMIUM"})
    private String depositRateName;

    @Schema(description = "자유 납입 횟수별 우대금리",
            example = "LEVEL2",
            allowableValues = {"NONE", "LEVEL1", "LEVEL2", "LEVEL3"})
    private String freeDepositCountRateName;


    // 엔티티 변환 메서드
    public PreferentialRateCondition toEntity() {
        AgePreferentialRate ageRate = convertToEnum(ageRateName, AgePreferentialRate.class);
        DepositPreferentialRate depositRate = convertToEnum(depositRateName, DepositPreferentialRate.class);
        FreeDepositCountPreferentialRate freeDepositCountRate = convertToEnum(freeDepositCountRateName, FreeDepositCountPreferentialRate.class);

        return PreferentialRateCondition.builder()
                .ageRate(ageRate)
                .depositRate(depositRate)
                .freeDepositCountRate(freeDepositCountRate)
                .build();
    }


    /**
     * 문자열을 해당 Enum 타입으로 변환
     * @param enumName 변환할 enum 이름 문자열
     * @param enumClass 대상 Enum 클래스
     * @return 변환된 Enum 또는 null
     */
    private <T extends Enum<T>> T convertToEnum(String enumName, Class<T> enumClass) {
        if (enumName == null || enumName.isEmpty()) {
            return null;
        }

        try {
            return Enum.valueOf(enumClass, enumName);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid enum name: {} for enum type: {}", enumName, enumClass.getSimpleName());
            return null;
        }
    }



}
