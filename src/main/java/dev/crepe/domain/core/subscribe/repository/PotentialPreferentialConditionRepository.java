package dev.crepe.domain.core.subscribe.repository;

import dev.crepe.domain.core.subscribe.model.entity.PotentialPreferentialCondition;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PotentialPreferentialConditionRepository extends JpaRepository<PotentialPreferentialCondition, Long> {

    List<PotentialPreferentialCondition> findBySubscribe(Subscribe subscribe);

}
