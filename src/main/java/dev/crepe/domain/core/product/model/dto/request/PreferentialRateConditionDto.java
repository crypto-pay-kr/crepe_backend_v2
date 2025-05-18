package dev.crepe.domain.core.product.model.dto.request;

import dev.crepe.domain.core.product.model.dto.interest.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferentialRateConditionDto {
    @Schema(description = "연령대별 우대금리",
            example = "YOUTH",
            allowableValues = {"YOUTH", "MIDDLE_AGED", "SENIOR", "ALL_AGES"})
    private List<String> ageRateNames;

    @Schema(description = "예치금액별 우대금리 목록 (여러 개 선택 가능)",
            example = "[\"MEDIUM\", \"LARGE\"]",
            allowableValues = {"SMALL", "MEDIUM", "LARGE", "PREMIUM"})
    private List<String>  depositRateNames;

    @Schema(description = "자유 납입 횟수별 우대금리 목록 (여러 개 선택 가능)",
            example = "[\"LEVEL1\", \"LEVEL2\"]",
            allowableValues = {"NONE", "LEVEL1", "LEVEL2", "LEVEL3"})
    private List<String>  freeDepositCountRateNames;


    public List<AgePreferentialRate> getAgeRates() {
        if (ageRateNames == null || ageRateNames.isEmpty()) {
            return new ArrayList<>();
        }
        return convertStringListToEnumList(ageRateNames, AgePreferentialRate.class);
    }

    public List<DepositPreferentialRate> getDepositRates() {
        if (depositRateNames == null || depositRateNames.isEmpty()) {
            return new ArrayList<>();
        }
        return convertStringListToEnumList(depositRateNames, DepositPreferentialRate.class);
    }

    public List<FreeDepositCountPreferentialRate> getFreeDepositCountRates() {
        if (freeDepositCountRateNames == null || freeDepositCountRateNames.isEmpty()) {
            return new ArrayList<>();
        }
        return convertStringListToEnumList(freeDepositCountRateNames, FreeDepositCountPreferentialRate.class);
    }


    private <T extends Enum<T>> List<T> convertStringListToEnumList(List<String> stringList, Class<T> enumClass) {
        List<T> enumList = new ArrayList<>();

        for (String enumName : stringList) {
            if (enumName != null && !enumName.isEmpty()) {
                try {
                    T enumValue = Enum.valueOf(enumClass, enumName);
                    enumList.add(enumValue);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid enum name: {} for enum type: {}", enumName, enumClass.getSimpleName());
                }
            }
        }

        return enumList;
    }




}
