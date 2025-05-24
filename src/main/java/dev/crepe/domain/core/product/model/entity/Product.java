package dev.crepe.domain.core.product.model.entity;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "product")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BankProductType type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "budget", precision = 20, scale = 8)
    private BigDecimal budget;

    // duration 필드 제거하고 시작일/종료일 추가
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // 가입대상
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

    @Column(name="product_image")
    private String imageUrl;

    @Column(name="product_guide_image")
    private String guideFileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BankProductStatus status;

    // 반려사유
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;


    @Formula("(SELECT COUNT(*) FROM subscribe s WHERE s.product_id = id AND s.status = 'ACTIVE')")
    private Integer subscribeCount;

    // 우대 금리, 조건
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PreferentialInterestCondition> preferentialConditions = new ArrayList<>();


    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductTag> productTags = new ArrayList<>();


    public void addTag(Tag tag) {
        if (this.productTags == null) {
            this.productTags = new ArrayList<>();
        }

        boolean exists = productTags.stream()
                .anyMatch(pt -> pt.getTag().equals(tag));

        if (!exists) {
            ProductTag productTag = ProductTag.create(this, tag);
            this.productTags.add(productTag);
        }
    }




    public void addPreferentialCondition(PreferentialInterestCondition condition) {
        if (this.preferentialConditions == null) {
            this.preferentialConditions = new ArrayList<>();
        }
        condition.setProduct(this); // 양방향 관계 설정
        this.preferentialConditions.add(condition);
    }

    public void updateStatus(BankProductStatus status, String reason) {
        this.status = status;

        if (status == BankProductStatus.APPROVED) {
            this.rejectionReason = null;
        } else if (status == BankProductStatus.REJECTED) {
            this.rejectionReason = reason;
        } else if( status == BankProductStatus.SUSPENDED) {
            this.rejectionReason = reason;
        }
    }
}

