package dev.crepe.domain.core.subscribe.service;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.product.model.entity.PreferentialInterestCondition;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;


public interface PreferentialConditionSatisfactionService {
    void recordImmediateSatisfaction(Actor user, Subscribe subscribe,
                                     PreferentialInterestCondition condition);

}
