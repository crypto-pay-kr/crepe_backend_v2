package dev.crepe.domain.core.util.history.transfer.model.entity;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.util.history.transfer.model.TransactionStatus;
import dev.crepe.domain.core.util.history.transfer.model.TransactionType;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_history")
public class TransactionHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "amount", precision = 20, scale = 8, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;  // 대기, 성공, 실패

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "transferred_at")
    private LocalDateTime transferredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;  // 입금, 출금, 이체, 이자지급, 정산, 결제

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

}