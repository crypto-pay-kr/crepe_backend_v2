package dev.crepe.domain.channel.market.order.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.crepe.domain.channel.market.menu.model.entity.Menu;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_detail")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int menuCount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

}
