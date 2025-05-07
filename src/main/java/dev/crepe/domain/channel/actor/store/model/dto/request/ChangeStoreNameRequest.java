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
@Schema(description = "가맹점 이름 변경 DTO")
public class ChangeStoreNameRequest {
    @Schema(description = "새로운 가맹점 이름", example = "크레페 하우스 강남점")
    private String newStoreName;
}
