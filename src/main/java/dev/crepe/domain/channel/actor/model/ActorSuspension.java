package dev.crepe.domain.channel.actor.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorSuspension {
    private SuspensionType type;
    private LocalDateTime suspendedAt;
    private LocalDateTime suspendedUntil;
    private String reason;
}
