package dev.crepe.domain.channel.actor.store.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.crepe.domain.channel.actor.store.model.StoreStatus;
import dev.crepe.domain.channel.market.like.model.entity.Like;
import dev.crepe.domain.channel.market.menu.model.entity.Menu;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.global.base.UserBaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="store")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Store extends UserBaseEntity {
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String address;

    @Column(name="business_number", nullable = false)
    private String businessNumber;

    @Column(name="business_image", nullable = false)
    private String businessImage;

    @Column(name="shop_image", nullable = false)
    private String shopImage;

    @ManyToMany
    @JoinTable(
            name = "store_coin",
            joinColumns = @JoinColumn(name = "store_id"),
            inverseJoinColumns = @JoinColumn(name = "coin_id")
    )
    private List<Coin> coinList = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private StoreStatus status = StoreStatus.CLOSED;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Menu> menus;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private List<Like> likes;
}
