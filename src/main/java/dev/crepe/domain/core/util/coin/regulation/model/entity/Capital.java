package dev.crepe.domain.core.util.coin.regulation.model.entity;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.product.model.entity.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


// 자본금 -> 은행의 예치금 정보
@Entity
@Table(name = "capital")
public class Capital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Actor user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "total_deposit_amount", precision = 20, scale = 8, nullable = false)
    private BigDecimal totalDepositAmount;

    @Column(name = "collateral_amount", precision = 20, scale = 8, nullable = false)
    private BigDecimal collateralAmount;

    @Column(name = "collateral_ratio", nullable = false)
    private Float collateralRatio;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


}