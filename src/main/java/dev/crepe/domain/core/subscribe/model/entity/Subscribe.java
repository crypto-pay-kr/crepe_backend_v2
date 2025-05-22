package dev.crepe.domain.core.subscribe.model.entity;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.product.model.dto.interest.FreeDepositCountPreferentialRate;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subscribe")
public class Subscribe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자와의 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Actor user;

    // 상품과의 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscribeStatus status;

    // 가입일
    @Column(name = "subscribeDate", nullable = false)
    private LocalDateTime subscribeDate;

    // 만기일(일단 지금 1년으로 fix)
    @Column(name = "expiredDate", nullable = false)
    private LocalDateTime expiredDate;

    // 현재 잔액
    @Column(name="balance", precision = 18, scale = 8, nullable = false)
    private BigDecimal balance;

    // 이자율
    @Column(name = "interestRate")
    private float interestRate;

    // 적용된 우대금리 정보 (JSON 문자열)
    @Column(name = "applied_preferential_rates", columnDefinition = "TEXT")
    private String appliedPreferentialRates;


    @Column(name = "regular_deposit_amount")
    private BigDecimal regularDepositAmount;
  
    // 다음 정기납입 예정일 (적금 상품인 경우)
    @Column(name = "next_regular_deposit_date")
    private LocalDate nextRegularDepositDate;

    // 중도해지 기준 세전 이자
    @Column(name = "preTaxInterest")
    private BigDecimal preTaxInterest;


    // 자유납입 목표 (적금 상품인 경우)
    @Enumerated(EnumType.STRING)
    @Column(name = "selected_free_deposit_rate")
    private FreeDepositCountPreferentialRate selectedFreeDepositRate;

    // 현재 월의 자유납입 횟수 (적금 상품인 경우)
    @Column(name = "current_month_deposit_count")
    private Integer currentMonthDepositCount;

    // 상품권 관련 필드
    // 상품권 코드 (상품권 상품인 경우)
    @Column(name = "voucher_code")
    private String voucherCode;



    /**
     * 예치 (잔액 증가)
     */
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("예치 금액은 0보다 커야 합니다.");
        }
        this.balance = this.balance.add(amount);
    }


    public void changeExpired() {
        this.balance = BigDecimal.ZERO;
        this.status = SubscribeStatus.EXPIRED;
    }

    public boolean isActive() {
        return this.status == SubscribeStatus.ACTIVE;
    }


    public boolean isMatured() {
        return LocalDateTime.now().isAfter(this.expiredDate);
    }

    public void setPreTaxInterest(BigDecimal preTaxInterest) {
        this.preTaxInterest = preTaxInterest;
    }
}
