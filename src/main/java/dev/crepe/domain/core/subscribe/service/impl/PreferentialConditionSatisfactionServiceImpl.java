package dev.crepe.domain.core.subscribe.service.impl;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.subscribe.model.entity.PreferentialConditionSatisfaction;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.PreferentialConditionSatisfactionRepository;
import dev.crepe.domain.core.subscribe.service.PreferentialConditionSatisfactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class PreferentialConditionSatisfactionServiceImpl implements PreferentialConditionSatisfactionService {
    private final PreferentialConditionSatisfactionRepository satisfactionRepository;

    @Override
    public void recordImmediateSatisfaction(Actor user, Subscribe subscribe, PreferentialInterestCondition condition) {
        // 중복 기록 방지
        if (satisfactionRepository.existsBySubscribeAndCondition(subscribe, condition)) {
            return;
        }

        PreferentialConditionSatisfaction satisfaction =
                PreferentialConditionSatisfaction.createImmediateSatisfaction(user, subscribe, condition);

        satisfactionRepository.save(satisfaction);

        log.info("즉시 충족 조건 기록: Subscribe ID {}, Condition: {}",
                subscribe.getId(), condition.getTitle());
    }



}
