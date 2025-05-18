package dev.crepe.domain.core.product.repository;

import dev.crepe.domain.core.product.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
  
}
