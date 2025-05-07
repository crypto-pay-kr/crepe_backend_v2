package dev.crepe.domain.core.account.model.entity;


import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Actor user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_token_id", nullable = false)
    private BankToken bankToken;

    @Column(name = "balance", precision = 20, scale = 8, nullable = false)
    private BigDecimal balance;

    @Column(name = "account_address", length = 255)
    private String accountAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


}