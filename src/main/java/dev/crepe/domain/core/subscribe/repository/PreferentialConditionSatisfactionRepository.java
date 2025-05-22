package dev.crepe.domain.core.subscribe.repository;

import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.subscribe.model.entity.PreferentialConditionSatisfaction;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;


@Repository
public interface PreferentialConditionSatisfactionRepository extends JpaRepository<PreferentialConditionSatisfaction, Long> {
    // 중복 기록 방지를 위한 존재 여부 확인
    boolean existsBySubscribeAndCondition(Subscribe subscribe, PreferentialInterestCondition condition);

  List<PreferentialConditionSatisfaction> findBySubscribe_IdAndIsSatisfiedTrue(Long subscribeId);

}
