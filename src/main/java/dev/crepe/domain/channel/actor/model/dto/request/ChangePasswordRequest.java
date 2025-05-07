package dev.crepe.domain.channel.actor.model.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 변경 요청 DTO")
public class ChangePasswordRequest {

    @Schema(description = "기존 비밀번호", example = "OldPassword123!")
    private String oldPassword;

    @Schema(description = "새 비밀번호", example = "NewPassword456@")
    private String newPassword;
}