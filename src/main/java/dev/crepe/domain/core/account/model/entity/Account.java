package dev.crepe.domain.core.account.model.entity;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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
    @Column(name = "address_status", length = 50, nullable = false)
    private AddressRegistryStatus addressRegistryStatus= AddressRegistryStatus.NOT_REGISTERED;

    @Builder.Default
    @Column(name = "non_available_balance", precision = 20, scale = 8, nullable = false)
    private BigDecimal nonAvailableBalance = BigDecimal.ZERO;

    @Column(name="tag")
    private String tag;

    // 유저 - 계좌 등록
    public void registerAddress(String address, String tag) {
        this.accountAddress = address;
        this.tag = tag;
        this.addressRegistryStatus = AddressRegistryStatus.REGISTERING;
    }

    // 유저 - 계좌 해제 요청 후 계좌 등록 요청
    public void reRegisterAddress(String address, String tag) {
        this.accountAddress = address;
        this.tag = tag;
        this.addressRegistryStatus = AddressRegistryStatus.UNREGISTERED_AND_REGISTERING;
    }

    // 유저 - 계좌 해지 요청
    public void unRegisterAddress() {
        this.addressRegistryStatus = AddressRegistryStatus.UNREGISTERED;
    }

    // 어드민 - 계좌 승인
    public void approveAddress() {
        this.addressRegistryStatus = AddressRegistryStatus.ACTIVE;
    }

    // 어드민 - 계좌 해지
    public void adminUnRegisterAddress() {
        this.addressRegistryStatus = AddressRegistryStatus.NOT_REGISTERED;
        this.accountAddress = null;
        this.tag = null;
    }

    // 어드민 - 계좌 등록 요청 거절
    public void adminRejectAddress() {
        this.addressRegistryStatus = AddressRegistryStatus.REJECTED;
    }

    // 어드민 - 계좌 정지
    public void adminHoldAddress() { this.addressRegistryStatus = AddressRegistryStatus.HOLD; }

    public void addNonAvailableBalance(BigDecimal amount) {
        this.nonAvailableBalance = this.nonAvailableBalance.add(amount);
    }


    public void deductBalance(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }


    public void reduceAmount(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void reduceNonAvailableBalance(BigDecimal amount) {
        this.nonAvailableBalance = this.nonAvailableBalance.subtract(amount);
    }

    public void addAmount(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

}