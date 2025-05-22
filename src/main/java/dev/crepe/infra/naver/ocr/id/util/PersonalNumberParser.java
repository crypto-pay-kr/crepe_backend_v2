package dev.crepe.infra.naver.ocr.id.util;

import dev.crepe.domain.channel.actor.model.Gender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersonalNumberParser {

    /**
     * 주민번호에서 생년월일을 YYYYMMDD 형식으로 파싱
     * @param personalNum 주민번호 (예: 000426-4122342)
     * @return 생년월일 (예: 20000426)
     */
    public static String parseBirthDate(String personalNum) {
        if (personalNum == null || personalNum.trim().isEmpty()) {
            throw new IllegalArgumentException("주민번호가 없습니다.");
        }

        // 하이픈과 공백 모두 제거
        String cleanedNum = personalNum.replace("-", "").replace(" ", "").trim();
        log.info(cleanedNum);
        if (cleanedNum.length() < 7) {
            throw new IllegalArgumentException("주민번호 형식이 올바르지 않습니다.");
        }

        try {
            // 앞 6자리에서 생년월일 추출 (YYMMDD)
            String year = cleanedNum.substring(0, 2);
            String month = cleanedNum.substring(2, 4);
            String day = cleanedNum.substring(4, 6);
            String genderCode = cleanedNum.substring(6, 7);

            // 연도 계산 (1,2는 1900년대, 3,4는 2000년대)
            int fullYear;
            if (genderCode.equals("1") || genderCode.equals("2")) {
                fullYear = 1900 + Integer.parseInt(year);
            } else if (genderCode.equals("3") || genderCode.equals("4")) {
                fullYear = 2000 + Integer.parseInt(year);
            } else {
                throw new IllegalArgumentException("성별 코드가 올바르지 않습니다.");
            }

            // YYYY-MM-DD 형태로 반환
            return String.format("%04d-%s-%s", fullYear, month, day);

        } catch (Exception e) {
            throw new IllegalArgumentException("주민번호 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 주민번호에서 성별 파싱
     * @param personalNumber 주민번호 (예: 000426-4122342)
     * @return Gender enum
     */
    public static Gender parseGender(String personalNumber) {
        return Gender.fromPersonalNumber(personalNumber);
    }



}
