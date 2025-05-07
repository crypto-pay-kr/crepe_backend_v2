package dev.crepe.domain.channel.actor.store.model.dto.request;

import dev.crepe.domain.channel.actor.store.model.StoreStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "가맹점 영업 상태 수정 요청 DTO")
public class ChangeStoreStatusRequest {
    @Schema(description = "영업중(OPEN), 영업종료(CLOSE)", example = "OPEN")
    private StoreStatus storeStatus;
}

