package dev.crepe.domain.core.util.history.exchange.model.entity;

import dev.crepe.domain.core.account.model.entity.Account;
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
@Table(name = "exchange_history")
public class ExchangeHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_account", nullable = false)
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account", nullable = false)
    private Account toAccount;

    @Column(name = "from_exchange_rate", precision = 20, scale = 8)
    private BigDecimal fromExchangeRate;

    @Column(name = "to_exchange_rate", precision = 20, scale = 8)
    private BigDecimal toExchangeRate;

    @Column(name = "from_amount", precision = 20, scale = 8)
    private BigDecimal fromAmount;

    @Column(name = "to_amount", precision = 20, scale = 8)
    private BigDecimal toAmount;

    @Column(name = "after_token_balance_from", precision = 20, scale = 8)
    private BigDecimal afterTokenBalanceFrom;

    @Column(name = "after_token_balance_to", precision = 20, scale = 8)
    private BigDecimal afterTokenBalanceTo;

    @Column(name = "after_coin_balance_from", precision = 20, scale = 8)
    private BigDecimal afterCoinBalanceFrom;

    @Column(name = "after_coin_balance_to", precision = 20, scale = 8)
    private BigDecimal afterCoinBalanceTo;

}