package dev.crepe.domain.channel.actor.store.model.dto.request;


import dev.crepe.domain.channel.actor.store.model.PreparationTime;
import dev.crepe.domain.channel.actor.store.model.RefusalReason;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "주문 관리 요청 DTO")
public class StoreOrderActionRequest {

    @Schema(description = "주문 액션 (accept, refuse, complete)", example = "accept")
    private String action; // "accept", "refuse", "complete"
    @Schema(description = "준비 시간 (주문 수락 시 사용)", example = "30분", nullable = true)
    private PreparationTime preparationTime; // 주문 접수 시 사용
    @Schema(description = "거절 사유 (주문 거절 시 사용)", example = "재고없음", nullable = true)
    private RefusalReason refusalReason; // 주문 거절 시 사용


}
