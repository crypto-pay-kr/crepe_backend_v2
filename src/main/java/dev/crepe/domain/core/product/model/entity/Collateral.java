package dev.crepe.domain.core.product.model.entity;



import dev.crepe.domain.channel.actor.user.model.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


// 담보금
@Entity
@Table(name = "collateral")
public class Collateral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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