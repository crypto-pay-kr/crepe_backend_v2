package dev.crepe.domain.channel.actor.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("남성"),
    FEMALE("여성");

    private final String description;

    public static Gender fromPersonalNumber(String personalNumber) {
        if (personalNumber == null || personalNumber.length() < 8) {
            throw new IllegalArgumentException("유효하지 않은 주민번호입니다.");
        }

        // 주민번호에서 성별 구분 숫자 추출 (7번째 자리)
        String[] parts = personalNumber.split("-");
        if (parts.length != 2 || parts[1].length() < 1) {
            throw new IllegalArgumentException("유효하지 않은 주민번호 형식입니다.");
        }

        char genderDigit = parts[1].charAt(0);

        // 성별 구분: 1,3 -> 남성, 2,4 -> 여성
        switch (genderDigit) {
            case '1': case '3':
                return MALE;
            case '2': case '4':
                return FEMALE;
            default:
                throw new IllegalArgumentException("유효하지 않은 성별 구분 숫자입니다: " + genderDigit);
        }
    }
}
