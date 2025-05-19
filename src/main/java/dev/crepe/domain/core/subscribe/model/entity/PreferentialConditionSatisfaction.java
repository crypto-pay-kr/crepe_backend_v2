package dev.crepe.domain.core.subscribe.model.entity;


import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.subscribe.model.SatisfactionType;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 우대 조건 충족 이력
 * - 사용자가 특정 우대 조건을 충족했을 때의 기록
 * - 가입 시점에 즉시 충족된 조건과 나중에 충족된 조건 모두 기록
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "preferential_condition_satisfaction")
public class PreferentialConditionSatisfaction extends BaseEntity {

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

    @Column(name = "is_satisfied", nullable = false)
    private boolean isSatisfied;

    @Column(name = "evaluated_at", nullable = false)
    private LocalDateTime evaluatedAt;

    /** 적용된 우대금리 (%) */
    @Column(name = "applied_rate")
    private Float appliedRate;

    /** 조건 충족 방식 (즉시/잠재적) */
    @Enumerated(EnumType.STRING)
    @Column(name = "satisfaction_type")
    private SatisfactionType satisfactionType;

    /** 조건 충족 시의 상세 정보 (JSON) */
    @Column(name = "satisfaction_details", columnDefinition = "TEXT")
    private String satisfactionDetails;

    // 팩토리 메서드
    public static PreferentialConditionSatisfaction createImmediateSatisfaction(
            Actor user, Subscribe subscribe, PreferentialInterestCondition condition) {
        return PreferentialConditionSatisfaction.builder()
                .user(user)
                .subscribe(subscribe)
                .condition(condition)
                .isSatisfied(true)
                .evaluatedAt(LocalDateTime.now())
                .appliedRate(condition.getRate())
                .satisfactionType(SatisfactionType.IMMEDIATE)
                .build();
    }

    public static PreferentialConditionSatisfaction createPotentialSatisfaction(
            Actor user, Subscribe subscribe, PreferentialInterestCondition condition,
            String satisfactionDetails) {
        return PreferentialConditionSatisfaction.builder()
                .user(user)
                .subscribe(subscribe)
                .condition(condition)
                .isSatisfied(true)
                .evaluatedAt(LocalDateTime.now())
                .appliedRate(condition.getRate())
                .satisfactionType(SatisfactionType.POTENTIAL)
                .satisfactionDetails(satisfactionDetails)
                .build();
    }
}

