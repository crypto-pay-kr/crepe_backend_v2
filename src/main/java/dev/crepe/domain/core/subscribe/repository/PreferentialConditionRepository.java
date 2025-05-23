package dev.crepe.domain.core.subscribe.repository;

import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.subscribe.model.entity.PreferentialConditionSatisfaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferentialConditionRepository extends JpaRepository<PreferentialInterestCondition,Long> {
    Optional<PreferentialInterestCondition> findByProductAndTitle(Product product, String title);

}
