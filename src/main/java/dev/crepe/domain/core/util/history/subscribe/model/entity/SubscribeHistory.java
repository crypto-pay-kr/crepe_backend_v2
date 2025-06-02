package dev.crepe.domain.core.util.history.subscribe.model.entity;

import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.util.history.subscribe.model.SubscribeHistoryType;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Entity
@Builder
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "subscribe_history")
public class SubscribeHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관된 가입 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscribe_id", nullable = false)
    private Subscribe subscribe;

    // 이벤트 타입: 이자지급, 예치, 소비, 해지 등
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private SubscribeHistoryType eventType;

    // 금액 (이자: +, 해지: -, 소비: - 등)
    @Column(name = "amount", precision = 18, scale = 8, nullable = false)
    private BigDecimal amount;

    @Column(name = "after_balance", precision = 18, scale = 8)
    private BigDecimal afterBalance;

    @Column(name = "after_account_balance", precision = 18, scale = 8)
    private BigDecimal afterAccountBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

}
