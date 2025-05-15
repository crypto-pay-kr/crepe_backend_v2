package dev.crepe.domain.core.util.history.exchange.model.entity;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
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

    @Column(name = "after_balance_from", precision = 20, scale = 8)
    private BigDecimal afterBalanceFrom;

    @Column(name = "after_balance_to", precision = 20, scale = 8)
    private BigDecimal afterBalanceTo;

}