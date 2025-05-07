package dev.crepe.domain.channel.market.menu.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="menu")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Menu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    @JsonIgnore
    private Actor store;

    public void updateMenu(String name, int price, String image) {
        this.name = name;
        this.price = price;
        this.image = image;
    }
    public void delete() {
        setDataStatus(false);
    }
}
