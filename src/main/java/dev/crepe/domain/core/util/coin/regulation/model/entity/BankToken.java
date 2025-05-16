package dev.crepe.domain.core.util.coin.regulation.model.entity;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.history.token.model.entity.TokenPortfolioHistory;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name = "bank_token")
@NoArgsConstructor
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

    //총 발행량 -> ex. 100만원(고정값)
    @Column(name = "total_supply", precision = 20, scale = 8, nullable = false)
    private BigDecimal totalSupply;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BankTokenStatus status;

    @OneToMany(mappedBy = "bankToken", fetch = FetchType.LAZY)
    private List<TokenPortfolioHistory> tokenHistories;

    @OneToMany(mappedBy = "bankToken", fetch = FetchType.LAZY)
    private List<Portfolio> portfolios;


    public void updateStatus(BankTokenStatus status) {
        this.status = status;
    }

}