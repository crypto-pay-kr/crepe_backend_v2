package dev.crepe.infra.sms.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SmsType {

    SIGN_UP(300, "회원 가입시에 전송됨"),
    FIND_ID(300, "아이디 찾기 시에 전송됨"),
    RESET_PWD(300, "비밀번호 재설정 시에 전송됨"),
    CHANGE_PHONE_NUM(300, "휴대폰 번호 변경 시에 전송됨");

    private final int expirationSecond;
    private final String description;
}
