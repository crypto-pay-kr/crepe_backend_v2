package dev.crepe.domain.core.product.model.entity;

import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    @JoinColumn(name = "bank_id", nullable = false)
    private List<Product> products = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_id", nullable = false)
    private BankToken bankToken;

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

    @Column(name = "duration")
    private Integer duration;

    //기본금리
    @Column(name = "base_interest_rate")
    private Float baseInterestRate;

    //우대금리
    @Column(name = "preferred_interest_rate")
    private Float preferredInterestRate;

    //월 최대 입금액
    @Column(name = "min_monthly_payment", precision = 20, scale = 8)
    private BigDecimal maxMonthlyPayment;

    //최대 가입 인원
    @Column(name = "max_participants")
    private Integer maxParticipants;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

}