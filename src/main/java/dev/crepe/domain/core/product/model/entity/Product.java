package dev.crepe.domain.core.product.model.entity;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "bank_product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_token_id", nullable = false)
    private BankToken bankToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    // 은행 상품 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BankProductType type;

    // 은행 상품 승인 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BankProductStatus status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "budget", precision = 20, scale = 8)
    private BigDecimal budget;

    // duration 필드 제거하고 시작일/종료일 추가
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    // 가입조건
    @Column(name = "join_condition", columnDefinition = "TEXT")
    private String joinCondition;

    //기본금리
    @Column(name = "base_interest_rate")
    private Float baseInterestRate;

    //월 최대 입금액
    @Column(name = "max_monthly_payment", precision = 20, scale = 8)
    private BigDecimal maxMonthlyPayment;

    //최대 가입 인원
    @Column(name = "max_participants")
    private Integer maxParticipants;

    // 우대 금리, 조건
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PreferentialInterestCondition> preferentialConditions = new ArrayList<>();

}