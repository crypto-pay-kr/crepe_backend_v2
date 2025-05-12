package dev.crepe.domain.core.util.coin.global.model.entity;


import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 포토폴리오 -> 자본금 내  코인 구성정보
@Entity
@Table(name = "coin-portfolio")
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_id", nullable = false)
    private BankToken bankToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;

    @Column(name = "amount", precision = 20, scale = 8, nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}