package dev.crepe.domain.core.util.history.exchange.model.entity;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "exchange_history")
public class ExchangeHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_currency", nullable = false)
    private Account fromCurrency;

    @ManyToOne
    @JoinColumn(name = "to_currency", nullable = false)
    private Account toCurrency;

    @Column(name = "from_exchange_rate", precision = 20, scale = 8)
    private BigDecimal fromExchangeRate;

    @Column(name = "to_exchange_rate", precision = 20, scale = 8)
    private BigDecimal toExchangeRate;

    @Column(name = "from_amount", precision = 20, scale = 8)
    private BigDecimal fromAmount;

    @Column(name = "to_amount", precision = 20, scale = 8)
    private BigDecimal toAmount;

}