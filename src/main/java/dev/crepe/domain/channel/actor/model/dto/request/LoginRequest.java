package dev.crepe.domain.channel.actor.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청 DTO")
public class LoginRequest {
    @NotNull
    @Schema(description = "이메일", example = "user@crepe.co.kr")
    private String email;

    @NotNull
    @Schema(description = "비밀번호", example = "CrepeUser123!")
    private String password;

    @Schema(description = "캡차 키", example = "6NcD7YkIZA")
    private String captchaKey;

    @Schema(description = "캡차 입력값", example = "a5x9z2")
    private String captchaValue;
}
