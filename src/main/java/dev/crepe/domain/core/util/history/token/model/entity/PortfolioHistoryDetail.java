package dev.crepe.domain.core.util.history.token.model.entity;

import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "portfolio_history_detail")
public class PortfolioHistoryDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 변경 이력(TokenHistory)에 속하는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_history_id")
    private TokenHistory tokenHistory;

    private String coinName;
    private String coinCurrency;

    private BigDecimal prevAmount;
    private BigDecimal prevPrice;


    private BigDecimal updateAmount;
    private BigDecimal updatePrice;

    @Builder
    public PortfolioHistoryDetail(TokenHistory tokenHistory, String coinName,
                                  String coinCurrency, BigDecimal prevAmount, BigDecimal prevPrice,
                                  BigDecimal updateAmount, BigDecimal updatePrice) {
        this.tokenHistory = tokenHistory;
        this.coinName = coinName;
        this.coinCurrency = coinCurrency;
        this.prevAmount = prevAmount;
        this.prevPrice = prevPrice;
        this.updateAmount = updateAmount;
        this.updatePrice = updatePrice;
    }
}