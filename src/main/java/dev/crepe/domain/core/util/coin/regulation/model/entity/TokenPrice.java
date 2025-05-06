package dev.crepe.domain.core.util.coin.regulation.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//토큰시세
@Entity
@Table(name = "token_price")
public class TokenPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_token_id", nullable = false)
    private BankToken bankToken;

    // 기준 가격
    @Column(name = "price", precision = 20, scale = 8, nullable = false)
    private BigDecimal price;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


}