package dev.crepe.domain.channel.market.like.model.entity;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "likes",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id","store_id"})})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Like extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = false;  // 좋아요 상태 (true: 활성, false: 비활성)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private Actor user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Actor store;

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}

