package dev.crepe.domain.channel.market.order.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.store.model.PreparationTime;
import dev.crepe.domain.channel.actor.store.model.dto.response.StoreOrderResponse;
import dev.crepe.domain.channel.market.order.model.OrderStatus;
import dev.crepe.domain.channel.market.order.model.OrderType;
import dev.crepe.domain.channel.market.order.util.OrderIdGenerator;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @Column(length = 12, nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "ready_at", nullable = true)
    private LocalDateTime readyAt; // 준비 완료 시간 (분 단위)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    @Column(nullable = false)
    private String currency;

    @Column(name = "exchange_rate", precision = 18, scale = 8, nullable = false)
    private BigDecimal exchangeRate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDetail> orderDetails =  new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private Actor user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    @JsonIgnore
    private Actor store;

    @PrePersist
    private void generateId() {
        this.id = OrderIdGenerator.generate();
    }

    // 주문 접수
    public void accept(PreparationTime preparationTime) {

        this.status = OrderStatus.PAID;
        this.readyAt = this.getUpdatedAt().plusMinutes(preparationTime.getMinutes());
    }

    // 주문 거절
    public void refuse() {
        this.status = OrderStatus.CANCELLED;
    }

    // 주문 완료
    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    public StoreOrderResponse toStoreOrderResponse() {
        return StoreOrderResponse.builder()
                .orderId(this.id)
                .totalPrice(this.totalPrice)
                .status(this.status)
                .orderType(this.type.name())
                .createdAt(this.getCreatedAt())
                .orderDetails(this.orderDetails.stream()
                        .map(detail -> StoreOrderResponse.OrderDetailResponse.builder()
                                .menuName(detail.getMenu().getName())
                                .menuCount(detail.getMenuCount())
                                .menuPrice(detail.getMenu().getPrice())
                                .build())
                        .toList())
                .build();
    }

}
