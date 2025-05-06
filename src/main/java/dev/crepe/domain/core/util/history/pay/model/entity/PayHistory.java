package dev.crepe.domain.core.util.history.pay.model.entity;

import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.util.history.pay.model.PayType;
import dev.crepe.domain.core.util.history.transfer.model.entity.TransactionHistory;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pay_history")
public class PayHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayType status;

    @Column(name = "total_amount", precision = 20, scale = 8, nullable = false)
    private BigDecimal totalAmount;

}