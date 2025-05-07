package dev.crepe.domain.channel.actor.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "일반 사용자 닉네임 변경 요청 DTO")
public class ChangeNicknameRequest {
    @Schema(description = "변경할 닉네임", example = "크레프헤이터")
    private String newNickname;
}
