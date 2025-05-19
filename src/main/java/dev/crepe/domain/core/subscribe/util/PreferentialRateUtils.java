package dev.crepe.domain.core.subscribe.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.product.model.dto.eligibility.AgeGroup;
import dev.crepe.domain.core.product.model.dto.eligibility.Occupation;
import dev.crepe.domain.core.product.model.dto.interest.FreeDepositCountPreferentialRate;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.subscribe.model.PreferentialRateModels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreferentialRateUtils {

    private final ObjectMapper objectMapper;

    /**
     * 연령 조건 체크
     */
    public boolean checkAgeCondition(Actor user, PreferentialInterestCondition condition) {
        if (user.getBirth() == null) {
            log.warn("User {} has no birth info", user.getId());
            return false;
        }

        int userAge = calculateAgeFromBirthString(user.getBirth());
        if (userAge == -1) {
            log.warn("Invalid birth format for user {}: {}", user.getId(), user.getBirth());
            return false;
        }

        String title = condition.getTitle().toLowerCase();

        // 조건별 나이 범위 체크
        if (title.contains("청년")) {
            return userAge >= 19 && userAge <= 34;
        } else if (title.contains("중장년")) {
            return userAge >= 35 && userAge <= 64;
        } else if (title.contains("노년") || title.contains("시니어")) {
            return userAge >= 65;
        } else if (title.contains("전연령") || title.contains("전체")) {
            return true;
        }

        // 더 세부적인 조건이 필요한 경우 condition.getDescription()을 파싱할 수도 있음
        log.debug("Unknown age condition for title: {}", condition.getTitle());
        return false;
    }

    /**
     * 예치금액 조건 체크
     */
    public boolean checkDepositAmountCondition(BigDecimal amount, PreferentialInterestCondition condition) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        String title = condition.getTitle().toLowerCase();

        // 금액 구간별 체크
        if (title.contains("소액")) {
            // 1천만원 미만
            return amount.compareTo(new BigDecimal("10000000")) < 0;
        } else if (title.contains("중액")) {
            // 1천만원 이상 5천만원 미만
            return amount.compareTo(new BigDecimal("10000000")) >= 0 &&
                    amount.compareTo(new BigDecimal("50000000")) < 0;
        } else if (title.contains("고액")) {
            // 5천만원 이상 1억원 미만
            return amount.compareTo(new BigDecimal("50000000")) >= 0 &&
                    amount.compareTo(new BigDecimal("100000000")) < 0;
        } else if (title.contains("프리미엄")) {
            // 1억원 이상
            return amount.compareTo(new BigDecimal("100000000")) >= 0;
        }

        // description에서 더 정확한 범위를 파싱할 수도 있음
        log.debug("Unknown deposit amount condition for title: {}", condition.getTitle());
        return false;
    }

    /**
     * 자유납입 조건 선택 여부 체크
     */
    public boolean checkFreeDepositSelection(FreeDepositCountPreferentialRate selectedRate,
                                             PreferentialInterestCondition condition) {
        if (selectedRate == null) {
            return false;
        }

        String title = condition.getTitle().toLowerCase();

        switch (selectedRate) {
            case NONE:
                return title.contains("없음") || title.contains("선택안함");
            case LEVEL1:
                return title.contains("초급") || title.contains("level1") || title.contains("3회");
            case LEVEL2:
                return title.contains("중급") || title.contains("level2") || title.contains("5회");
            case LEVEL3:
                return title.contains("고급") || title.contains("level3") || title.contains("10회");
            default:
                return false;
        }
    }

    /**
     * 직업 조건 체크
     */
    public boolean checkOccupationCondition(Actor user, PreferentialInterestCondition condition) {
        if (user.getOccupation() == null) {
            log.warn("User {} has no occupation", user.getId());
            return false;
        }

        Occupation userOccupation = user.getOccupation();
        String conditionTitle = condition.getTitle().toLowerCase();
        String conditionDesc = condition.getDescription().toLowerCase();

        // 전체 직업 허용
        if (conditionTitle.contains("전체") || conditionTitle.contains("제한없음") ||
                conditionDesc.contains("모든") || conditionDesc.contains("전체")) {
            return true;
        }

        switch (userOccupation) {
            case EMPLOYEE:
                return conditionTitle.contains("직장인") || conditionDesc.contains("직장인") ||
                        conditionTitle.contains("employee") || conditionDesc.contains("employee");

            case SELF_EMPLOYED:
                return conditionTitle.contains("자영업") || conditionDesc.contains("자영업") ||
                        conditionTitle.contains("사업자") || conditionDesc.contains("사업자");

            case PUBLIC_SERVANT:
                return conditionTitle.contains("공무원") || conditionDesc.contains("공무원") ||
                        conditionTitle.contains("공공기관") || conditionDesc.contains("공공기관");

            case MILITARY:
                return conditionTitle.contains("군인") || conditionDesc.contains("군인") ||
                        conditionTitle.contains("군") || conditionDesc.contains("군");

            case STUDENT:
                return conditionTitle.contains("학생") || conditionDesc.contains("학생") ||
                        conditionTitle.contains("대학생") || conditionDesc.contains("대학생");

            case HOUSEWIFE:
                return conditionTitle.contains("주부") || conditionDesc.contains("주부") ||
                        conditionTitle.contains("전업주부") || conditionDesc.contains("전업주부");

            case UNEMPLOYED:
                return conditionTitle.contains("무직") || conditionDesc.contains("무직") ||
                        conditionTitle.contains("구직자") || conditionDesc.contains("구직자");

            default:
                return false;
        }
    }

    /**
     * 적용된 우대금리 조건들을 JSON 문자열로 변환
     */
    public String buildAppliedRatesJson(List<PreferentialRateModels.AppliedCondition> confirmedConditions,
                                        List<PreferentialRateModels.AppliedCondition> potentialConditions) {
        try {
            Map<String, Object> ratesInfo = new HashMap<>();

            // 확정된 조건들
            List<Map<String, Object>> confirmedList = new ArrayList<>();
            for (PreferentialRateModels.AppliedCondition condition : confirmedConditions) {
                Map<String, Object> conditionMap = new HashMap<>();
                conditionMap.put("title", condition.getCondition().getTitle());
                conditionMap.put("description", condition.getCondition().getDescription());
                conditionMap.put("rate", condition.getAppliedRate());
                conditionMap.put("status", "CONFIRMED");
                confirmedList.add(conditionMap);
            }

            // 잠재적 조건들
            List<Map<String, Object>> potentialList = new ArrayList<>();
            for (PreferentialRateModels.AppliedCondition condition : potentialConditions) {
                Map<String, Object> conditionMap = new HashMap<>();
                conditionMap.put("title", condition.getCondition().getTitle());
                conditionMap.put("description", condition.getCondition().getDescription());
                conditionMap.put("rate", condition.getAppliedRate());
                conditionMap.put("status", "POTENTIAL");
                potentialList.add(conditionMap);
            }

            ratesInfo.put("confirmed", confirmedList);
            ratesInfo.put("potential", potentialList);
            ratesInfo.put("confirmedCount", confirmedList.size());
            ratesInfo.put("potentialCount", potentialList.size());

            return objectMapper.writeValueAsString(ratesInfo);

        } catch (JsonProcessingException e) {
            log.error("Failed to convert applied rates to JSON", e);
            return "{}";
        }
    }

    /**
     * JSON 문자열을 파싱하여 적용된 우대금리 정보 반환
     */
    public Map<String, Object> parseAppliedRatesJson(String appliedRatesJson) {
        try {
            if (appliedRatesJson == null || appliedRatesJson.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(appliedRatesJson, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse applied rates JSON: {}", appliedRatesJson, e);
            return new HashMap<>();
        }
    }

    /**
     * 주민등록번호 앞자리(생년월일)에서 나이 계산
     * @param birthString 생년월일 문자열 (예: "980426", "020315")
     * @return 만나이, 잘못된 형식의 경우 -1
     */
    public int calculateAgeFromBirthString(String birthString) {
        if (birthString == null || birthString.length() != 6) {
            return -1;
        }

        try {
            // 생년월일 파싱
            int year = Integer.parseInt(birthString.substring(0, 2));
            int month = Integer.parseInt(birthString.substring(2, 4));
            int day = Integer.parseInt(birthString.substring(4, 6));

            // 2000년대 vs 1900년대 판별
            // 00~29: 2000년대, 30~99: 1900년대 (일반적인 기준)
            int fullYear;
            if (year <= 29) {
                fullYear = 2000 + year;
            } else {
                fullYear = 1900 + year;
            }

            // LocalDate로 변환
            LocalDate birthDate = LocalDate.of(fullYear, month, day);

            // 만나이 계산
            return Period.between(birthDate, LocalDate.now()).getYears();

        } catch (Exception e) {
            log.error("Error parsing birth string: {}", birthString, e);
            return -1;
        }
    }

    public AgeGroup determineAgeGroup(String birthString) {
        int age = calculateAgeFromBirthString(birthString);

        if (age >= 19 && age <= 34) {
            return AgeGroup.YOUTH;
        } else if (age >= 35 && age <= 64) {
            return AgeGroup.MIDDLE_AGED;
        } else if (age >= 65) {
            return AgeGroup.SENIOR;
        } else {
            // 19세 미만인 경우 - 일반적으로 은행 상품 가입 제한
            return AgeGroup.YOUTH; // 또는 별도 처리
        }
    }

    /**
     * 우대금리 조건 유형 판별
     */
    public PreferentialRateType determineRateType(PreferentialInterestCondition condition) {
        String title = condition.getTitle().toLowerCase();
        String description = condition.getDescription().toLowerCase();

        // 연령별 우대금리
        if (title.contains("청년") || title.contains("중장년") || title.contains("노년") ||
                title.contains("시니어") || description.contains("세") || description.contains("나이")) {
            return PreferentialRateType.AGE;
        }

        // 예치금액별 우대금리
        if (title.contains("소액") || title.contains("중액") || title.contains("고액") ||
                title.contains("프리미엄") || description.contains("원") || description.contains("금액")) {
            return PreferentialRateType.DEPOSIT_AMOUNT;
        }

        // 자유납입 우대금리
        if (title.contains("자유납입") || title.contains("납입횟수") || title.contains("초급") ||
                title.contains("중급") || title.contains("고급") || description.contains("회")) {
            return PreferentialRateType.FREE_DEPOSIT;
        }

        // 직업별 우대금리
        if (title.contains("직장인") || title.contains("공무원") || title.contains("자영업") ||
                title.contains("학생") || description.contains("직업") || description.contains("직장")) {
            return PreferentialRateType.OCCUPATION;
        }

        return PreferentialRateType.UNKNOWN;
    }

    /**
     * 우대금리 적용 가능 여부 종합 체크
     */
    public boolean isConditionApplicable(Actor user, PreferentialInterestCondition condition,
                                         BigDecimal depositAmount,
                                         FreeDepositCountPreferentialRate selectedFreeDepositRate) {
        PreferentialRateType type = determineRateType(condition);

        switch (type) {
            case AGE:
                return checkAgeCondition(user, condition);
            case DEPOSIT_AMOUNT:
                return checkDepositAmountCondition(depositAmount, condition);
            case FREE_DEPOSIT:
                return checkFreeDepositSelection(selectedFreeDepositRate, condition);
            case OCCUPATION:
                return checkOccupationCondition(user, condition);
            case UNKNOWN:
            default:
                log.warn("Unknown preferential rate condition type for condition: {}", condition.getTitle());
                return false;
        }
    }

    /**
     * 우대금리 조건 유형 enum
     */
    public enum PreferentialRateType {
        AGE,           // 연령별
        DEPOSIT_AMOUNT, // 예치금액별
        FREE_DEPOSIT,  // 자유납입별
        OCCUPATION,    // 직업별
        UNKNOWN        // 알 수 없음
    }
}