package dev.crepe.domain.channel.actor.user.repository;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.global.base.CustomJpaRepository;

public interface UserRepository extends CustomJpaRepository<Actor, Long> {
    boolean existsByNickName(String nickName);
}
