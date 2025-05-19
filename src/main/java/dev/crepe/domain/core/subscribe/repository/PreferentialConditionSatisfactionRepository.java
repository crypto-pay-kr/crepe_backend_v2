package dev.crepe.domain.core.subscribe.repository;

import dev.crepe.domain.core.subscribe.model.entity.PreferentialConditionSatisfaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferentialConditionSatisfactionRepository extends JpaRepository<PreferentialConditionSatisfaction, Long> {
}
