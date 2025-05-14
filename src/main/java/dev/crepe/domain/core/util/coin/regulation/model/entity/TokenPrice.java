package dev.crepe.domain.core.util.coin.regulation.model.entity;

import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//토큰시세
@Entity
@Getter
@Table(name = "token_price")
public class TokenPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_token_id")
    private BankToken bankToken;

    // 기준 가격
    @Column(name = "price", precision = 20, scale = 8, nullable = false)
    private BigDecimal price;

}