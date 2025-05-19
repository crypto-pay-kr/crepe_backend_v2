package dev.crepe.domain.core.subscribe.model.entity;


import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.subscribe.model.PotentialType;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "potential_preferential_condition")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PotentialPreferentialCondition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Actor user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscribe subscribe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_id", nullable = false)
    private PreferentialInterestCondition condition;

    // 잠재적 조건 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "potential_type", nullable = false)
    private PotentialType potentialType;

    // 사용자가 설정한 목표값 (자유납입 등)
    @Column(name = "target_value")
    private String targetValue;

    // 현재 달성 상태
    @Column(name = "current_achievement", columnDefinition = "TEXT")
    private String currentAchievement;

    // 조건 체크 여부
    @Column(name = "is_monitoring", nullable = false)
    private boolean isMonitoring;

    // 마지막 체크 시간
    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    public static PotentialPreferentialCondition create(Actor user, Subscribe subscribe,
                                                        PreferentialInterestCondition condition,
                                                        PotentialType type, String targetValue) {
        return PotentialPreferentialCondition.builder()
                .user(user)
                .subscribe(subscribe)
                .condition(condition)
                .potentialType(type)
                .targetValue(targetValue)
                .isMonitoring(true)
                .build();
    }

    public void updateAchievement(String currentAchievement) {
        this.currentAchievement = currentAchievement;
        this.lastCheckedAt = LocalDateTime.now();
    }

    public void stopMonitoring() {
        this.isMonitoring = false;
    }
}