package dev.crepe.domain.core.util.history.transfer.model.entity;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import dev.crepe.domain.core.util.history.transfer.model.TransactionStatus;
import dev.crepe.domain.core.util.history.transfer.model.TransactionType;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


// 입금 출금 등 자금 거래 내역
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "transaction_history")
public class TransactionHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = true)
    private PayHistory payHistory;

    @Column(name = "amount", precision = 20, scale = 8, nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name="after_balance", precision=20,scale=8)
    private BigDecimal afterBalance;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;


    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    public void acceptedTransactionStatus() {
        this.status = TransactionStatus.ACCEPTED;
    }


    public void cancelTransactionType() {
        this.type = TransactionType.CANCEL;
    }

    public void updateAfterBalance(BigDecimal balance) {
        this.afterBalance = balance;
    }
    public void refundTransactionStatus() {
        this.status=TransactionStatus.REFUNDED;
    }

}