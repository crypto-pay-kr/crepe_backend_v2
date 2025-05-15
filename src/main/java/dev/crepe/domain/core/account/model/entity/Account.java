package dev.crepe.domain.core.account.model.entity;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.account.exception.InsufficientBalanceException;
import dev.crepe.domain.core.account.exception.NotEnoughAmountException;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.global.base.BaseEntity;
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
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Actor actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id")
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

    @Builder.Default
    @Column(name = "available_balance", precision = 20, scale = 8, nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name="tag")
    private String tag;

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

    // 계좌 승인 반려
    public void rejectAddress() {
        this.addressRegistryStatus = AddressRegistryStatus.REJECTED;
    }

    // 계좌 등록 대기중
    public void pendingAddress() { this.addressRegistryStatus = AddressRegistryStatus.REGISTERING;}


    public void allocateBudget(BigDecimal amount) {
        if (availableBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        this.availableBalance = this.availableBalance.subtract(amount);
    }

    public void releaseBudget(BigDecimal amount) {
        this.availableBalance = this.availableBalance.add(amount);
    }

    // 기존 메서드 수정
    public void reduceAmount(BigDecimal amount) {
        if (availableBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        this.balance = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
    }

    public void addAmount(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }
}