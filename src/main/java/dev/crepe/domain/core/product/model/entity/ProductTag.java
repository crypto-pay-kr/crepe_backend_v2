package dev.crepe.domain.core.product.model.entity;

import dev.crepe.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_tag")
@Getter
@NoArgsConstructor
public class ProductTag extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;


    public ProductTag(Product product, Tag tag) {
        this.product = product;
        this.tag = tag;
    }


    public static ProductTag create(Product product, Tag tag) {
        return new ProductTag(product, tag);
    }
}
