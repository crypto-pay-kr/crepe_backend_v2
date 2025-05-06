package dev.crepe.domain.core.util.coin.regulation.model.entity;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_token")
public class BankToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_id", nullable = false)
    private Bank bankId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "symbol", length = 5, nullable = false)
    private String symbol;

    @Column(name = "total_supply", precision = 20, scale = 8, nullable = false)
    private BigDecimal totalSupply;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BankTokenStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}