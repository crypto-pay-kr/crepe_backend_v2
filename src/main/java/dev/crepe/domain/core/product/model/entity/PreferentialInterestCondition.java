package dev.crepe.domain.core.product.model.entity;

import jakarta.persistence.*;


//우대 금리 조건
@Entity
@Table(name = "preferential_interest_condition")
public class PreferentialInterestCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관된 은행상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "percent", nullable = false)
    private Float percent;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

}
