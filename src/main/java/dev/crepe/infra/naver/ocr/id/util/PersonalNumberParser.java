package dev.crepe.infra.naver.ocr.id.util;

import dev.crepe.domain.channel.actor.model.Gender;

public class PersonalNumberParser {

    /**
     * 주민번호에서 생년월일을 YYYYMMDD 형식으로 파싱
     * @param personalNumber 주민번호 (예: 000426-4122342)
     * @return 생년월일 (예: 20000426)
     */
    public static String parseBirthDate(String personalNumber) {
        if (personalNumber == null || !personalNumber.matches("\\d{6}-\\d{7}")) {
            throw new IllegalArgumentException("유효하지 않은 주민번호 형식입니다: " + personalNumber);
        }

        String[] parts = personalNumber.split("-");
        String birthPart = parts[0];
        char genderDigit = parts[1].charAt(0);

        String year;
        switch (genderDigit) {
            case '1': case '2': // 1900년대 출생
                year = "19" + birthPart.substring(0, 2);
                break;
            case '3': case '4': // 2000년대 출생
                year = "20" + birthPart.substring(0, 2);
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 성별 구분 숫자입니다: " + genderDigit);
        }

        String month = birthPart.substring(2, 4);
        String day = birthPart.substring(4, 6);

        return year + month + day;
    }

    /**
     * 주민번호에서 성별 파싱
     * @param personalNumber 주민번호 (예: 000426-4122342)
     * @return Gender enum
     */
    public static Gender parseGender(String personalNumber) {
        return Gender.fromPersonalNumber(personalNumber);
    }

    /**
     * 생년월일 유효성 검증
     * @param birthDate YYYYMMDD 형식의 생년월일
     * @return 유효한지 여부
     */
    public static boolean isValidBirthDate(String birthDate) {
        if (birthDate == null || birthDate.length() != 8) {
            return false;
        }

        try {
            int year = Integer.parseInt(birthDate.substring(0, 4));
            int month = Integer.parseInt(birthDate.substring(4, 6));
            int day = Integer.parseInt(birthDate.substring(6, 8));

            // 기본적인 유효성 검증
            if (year < 1800 || year > 2100) return false;
            if (month < 1 || month > 12) return false;
            if (day < 1 || day > 31) return false;

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
