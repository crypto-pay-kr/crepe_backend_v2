package dev.crepe.domain.channel.actor.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "일반 사용자 회원가입 요청 DTO")
public class UserSignupRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @Schema(description = "이메일", example = "user@crepe.co.kr")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 10, message = "비밀번호는 10자 이상이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{10,}$",
            message = "비밀번호는 숫자, 대문자, 소문자, 특수문자를 포함해야 합니다."
    )
    @Schema(description = "비밀번호 (10자 이상, 숫자/대문자/소문자/특수문자 포함)", example = "CrepeUser123!")
    private String password;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^010\\d{8}$", message = "휴대폰 번호는 010으로 시작하는 11자리 숫자여야 합니다.")
    @Schema(description = "전화번호 (010으로 시작하는 11자리)", example = "01098765432")
    private String phoneNumber;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Schema(description = "사용자 실명", example = "김크레프")
    private String name;

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Schema(description = "사용자 닉네임", example = "크레프러버")
    private String nickname;

}
