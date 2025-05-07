package dev.crepe.domain.channel.actor.store.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "가맹점 주소 변경 DTO")
public class ChangeAddressRequest {
    @Schema(description = "새로운 가맹점 주소", example = "")
    private String newAddress;
}
