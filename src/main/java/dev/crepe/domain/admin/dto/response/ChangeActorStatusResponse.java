package dev.crepe.domain.admin.dto.response;

import dev.crepe.domain.channel.actor.model.ActorStatus;
import dev.crepe.domain.channel.actor.model.SuspensionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeActorStatusResponse {
    private Long userId;
    private String message;
    private ActorStatus actorStatus;
    private SuspensionInfo suspensionInfo; // 정지 상태일 때만 값 존재

    @Getter
    @Builder
    public static class SuspensionInfo {
        private SuspensionType type;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String reason;
    }
}
