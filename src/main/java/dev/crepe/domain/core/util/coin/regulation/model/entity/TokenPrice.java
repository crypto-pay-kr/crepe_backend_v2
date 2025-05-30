package dev.crepe.domain.core.util.coin.regulation.model.entity;

import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


//토큰시세
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "token_price")
public class TokenPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_token_id")
    private BankToken bankToken;

    // 토큰 시가총액
    @Column(name = "price", precision = 20, scale = 8, nullable = false)
    private BigDecimal price;

    // JSON 형식의 시세 정보
    @Column(name = "price_details", columnDefinition = "TEXT", nullable = true)
    private String priceDetails;

    // 등락율
    @Column(name = "change_rate_details", columnDefinition = "TEXT")
    private String changeRate;


}