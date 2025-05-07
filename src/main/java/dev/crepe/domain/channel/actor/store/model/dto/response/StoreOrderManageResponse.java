package dev.crepe.domain.channel.actor.store.model.dto.response;

import dev.crepe.domain.channel.market.order.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    @Schema(description = "응답 메시지", example = "30분 후 준비 예정입니다.")
    private String message;
}
