package dev.crepe.domain.core.product.model.dto.request;

import dev.crepe.domain.core.product.model.dto.interest.*;
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
    private String ageRateName;
    private String depositRateName;
    private String regularDepositRateName;
    private String freeDepositCountRateName;

    // 엔티티 변환 메서드
    public PreferentialRateCondition toEntity() {
        AgePreferentialRate ageRate = convertToEnum(ageRateName, AgePreferentialRate.class);
        DepositPreferentialRate depositRate = convertToEnum(depositRateName, DepositPreferentialRate.class);
        RegularDepositPreferentialRate regularDepositRate = convertToEnum(regularDepositRateName, RegularDepositPreferentialRate.class);
        FreeDepositCountPreferentialRate freeDepositCountRate = convertToEnum(freeDepositCountRateName, FreeDepositCountPreferentialRate.class);

        return PreferentialRateCondition.builder()
                .ageRate(ageRate)
                .depositRate(depositRate)
                .regularDepositRate(regularDepositRate)
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
