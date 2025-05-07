package dev.crepe.domain.channel.actor.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "휴대폰 번호 변경 요청 DTO")
public class ChangePhoneRequest {
    @Schema(description = "새 휴대폰 번호", example = "01098765433")
    private String phoneNumber;
}
