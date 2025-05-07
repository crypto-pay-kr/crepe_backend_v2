package dev.crepe.domain.channel.market.like.repository;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.market.like.model.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndStore(Actor user, Actor store);
    long countByStoreAndActiveTrue(Actor store);
}
