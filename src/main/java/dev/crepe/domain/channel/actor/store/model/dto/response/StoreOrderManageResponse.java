package dev.crepe.domain.channel.actor.store.model.dto.response;

import dev.crepe.domain.channel.market.order.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Schema(description = "주문 관리 응답 DTO")
public class StoreOrderManageResponse {

    @Schema(description = "주문 ID", example = "L9OZHXL721TA")
    private String orderId;
    @Schema(description = "주문 상태", example = "WAITING")
    private OrderStatus status;
    @Schema(description = "주문 수락 시간", example = "2023-10-01T11:00:00")
    private LocalDateTime updatedAt;
    @Schema(description = "준비 완료 예정 시간", example = "2023-10-01T12:00:00")
    private LocalDateTime ReadyAt;
    @Schema(description = "응답 메시지", example = "30분 후 준비 예정입니다.")
    private String message;
}
