package dev.crepe.domain.admin.dto.request;

import dev.crepe.domain.channel.actor.model.ActorStatus;
import dev.crepe.domain.channel.actor.model.SuspensionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Actor 계정 정지, 해제 DTO")
public class ChangeActorStatusRequest {

    @Schema(description = "대상 Actor ID")
    private Long actorId;

    @Schema(description = "수행할 액션", allowableValues = {"SUSPEND", "UNSUSPEND"})
    private String action;

    @Schema(description = "정지 설정 (정지 시에만 필요)", nullable = true)
    private SuspensionRequest suspensionRequest;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SuspensionRequest {
        @Schema(description = "정지 유형")
        private SuspensionType type;

        @Schema(description = "정지 일수 (임시정지 시)")
        private Integer days;

        @Schema(description = "정지 사유")
        private String reason;
    }
}
