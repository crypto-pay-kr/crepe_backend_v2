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
        if (personalNumber == null || personalNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 주민번호입니다.");
        }

        // 하이픈과 모든 공백 제거
        String cleanedNum = personalNumber.replace("-", "").replace(" ", "").trim();

        if (cleanedNum.length() < 7) {
            throw new IllegalArgumentException("유효하지 않은 주민번호 형식입니다.");
        }

        // 7번째 자리에서 성별 구분 숫자 추출
        char genderDigit = cleanedNum.charAt(6);

        // 성별 구분: 1,3 -> 남성, 2,4 -> 여성
        return switch (genderDigit) {
            case '1', '3' -> MALE;
            case '2', '4' -> FEMALE;
            default -> throw new IllegalArgumentException("유효하지 않은 성별 구분 숫자입니다: " + genderDigit);
        };
    }
}
