package dev.crepe.domain.core.subscribe.repository;

import dev.crepe.domain.core.subscribe.model.entity.PreferentialConditionSatisfaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferentialConditionSatisfactionRepository extends JpaRepository<PreferentialConditionSatisfaction, Long> {
  List<PreferentialConditionSatisfaction> findBySubscribe_IdAndIsSatisfiedTrue(Long subscribeId);
}
