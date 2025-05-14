package dev.crepe.domain.core.product.model.entity;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


// 자본금 -> 은행의 예치금 정보
@Entity
@Getter
@Table(name = "capital")
public class Capital extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @Column(name = "total_deposit_amount", precision = 20, scale = 8, nullable = false)
    private BigDecimal totalDepositAmount;

    @Column(name = "collateral_amount", precision = 20, scale = 8, nullable = false)
    private BigDecimal capitalAmount;

    @Column(name = "collateral_ratio", nullable = false)
    private Float capitalRatio;





}