package dev.crepe.domain.core.product.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


//우대 금리 조건
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
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

    @Column(name = "rate", nullable = false)
    private Float rate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    void setProduct(Product product) {
        this.product = product;
    }

    public static PreferentialInterestCondition create(String description, Float rate) {
        return PreferentialInterestCondition.builder()
                .description(description)
                .rate(rate)
                .build();
    }
}
