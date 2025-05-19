package dev.crepe.domain.core.subscribe.model.entity;


import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//우대 조건 충족
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
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


}