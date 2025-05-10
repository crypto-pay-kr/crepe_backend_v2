package dev.crepe.domain.core.account.model.entity;

import dev.crepe.domain.core.account.exception.NotEnoughAmountException;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.channel.actor.model.entity.Actor;
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
    private Actor actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_token_id")
    private BankToken bankToken;

    @Builder.Default
    @Column(name = "balance", precision = 20, scale = 8, nullable = false)
    private BigDecimal balance=BigDecimal.ZERO;

    @Column(name = "account_address", length = 255)
    private String accountAddress;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name="address_status")
    private AddressRegistryStatus addressRegistryStatus= AddressRegistryStatus.NOT_REGISTERED;


    @Column(name="tag")
    private String tag;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 계좌 등록
    public void registerAddress(String address, String tag) {
        this.accountAddress = address;
        this.tag = tag;
        this.addressRegistryStatus = AddressRegistryStatus.REGISTERING;
    }

    // 계좌 승인
    public void approveAddress() {
        this.addressRegistryStatus = addressRegistryStatus.ACTIVE;
    }
    // 금액 차감
    public void reduceAmount(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }
    // 금액 추가
    public void addAmount(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }


}