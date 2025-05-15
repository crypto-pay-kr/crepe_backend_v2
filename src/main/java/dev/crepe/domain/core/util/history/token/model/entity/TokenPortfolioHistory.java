package dev.crepe.domain.core.util.history.token.model.entity;

// 토큰 변경 및 상태 내역

import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "token_portfolio_history")
public class TokenPortfolioHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_token_id", nullable = false)
    private BankToken bankToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BankTokenStatus status;

    @Column(name = "amount", precision = 20, scale = 8, nullable = false)
    private BigDecimal amount;


    @Column(name = "decription", length = 100)
    private String description;


    public void approve() {
        this.status = BankTokenStatus.APPROVED;
    }

    public void reject() {this.status=BankTokenStatus.REJECTED;}

}
