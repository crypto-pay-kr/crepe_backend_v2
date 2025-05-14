package dev.crepe.domain.core.util.coin.regulation.model.entity;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "bank_token")
public class BankToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "currency", length = 5, nullable = false)
    private String currency;

    //총 발행량
    @Column(name = "total_supply", precision = 20, scale = 8, nullable = false)
    private BigDecimal totalSupply;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BankTokenStatus status;


}