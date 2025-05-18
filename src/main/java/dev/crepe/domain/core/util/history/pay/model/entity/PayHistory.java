package dev.crepe.domain.core.util.history.pay.model.entity;

import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.pay.model.PayType;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

//상품 주문후 결제 내역
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pay_history")
public class PayHistory extends BaseEntity {

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

    @Builder.Default
    @OneToMany(mappedBy = "payHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionHistory> transactionHistories = new ArrayList<>();

    public void addTransactionHistory(TransactionHistory tx) {
        if (!this.transactionHistories.contains(tx)) {
            this.transactionHistories.add(tx);
            tx.setPayHistory(this);
        }
    }

    public void approve() {
        this.status = PayType.APPROVED;
    }

    public void cancel() {
        this.status = PayType.CANCELED;
    }

    public void refund() {this.status=PayType.REFUND;}
}