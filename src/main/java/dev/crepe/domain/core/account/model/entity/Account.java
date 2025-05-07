package dev.crepe.domain.core.account.model.entity;

import dev.crepe.domain.channel.actor.store.model.entity.Store;
import dev.crepe.domain.channel.actor.user.model.entity.User;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store ;

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

    @Enumerated(EnumType.STRING)
    @Column(name="address_status")
    private AddressRegistryStatus addressRegistryStatus= AddressRegistryStatus.REGISTERING;


    @Column(name="tag")
    private String tag;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    public void approveAddress() {
        this.addressRegistryStatus = addressRegistryStatus.ACTIVE;
    }


    public void reduceAmount(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void addAmount(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }


}