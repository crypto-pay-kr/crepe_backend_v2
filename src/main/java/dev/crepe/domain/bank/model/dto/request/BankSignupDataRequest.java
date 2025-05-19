package dev.crepe.domain.bank.model.dto.request;

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
@Schema(description = "은행 회원가입 요청 DTO")
public class BankSignupDataRequest {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @Schema(description = "이메일", example = "bank@crepe.com")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 10, message = "비밀번호는 10자 이상이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{10,}$",
            message = "비밀번호는 숫자, 대문자, 소문자, 특수문자를 포함해야 합니다."
    )
    @Schema(description = "비밀번호 (10자 이상, 숫자/대문자/소문자/특수문자 포함)", example = "Bank12345!")
    private String password;

    @NotBlank(message = "은행명은 필수 입력값입니다.")
    @Schema(description = "은행명", example = "크레페 은행")
    private String name;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10~11자리 숫자여야 합니다.")
    @Schema(description = "전화번호", example = "0212345678")
    private String bankPhoneNum;

    @NotBlank(message = "은행 코드는 필수 입력값입니다.")
    @Schema(description = "은행 코드", example = "BANK123")
    private String bankCode;
}