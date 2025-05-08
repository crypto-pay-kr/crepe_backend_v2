package dev.crepe.domain.channel.actor.store.repository;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.store.model.StoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface StoreRepository extends JpaRepository<Actor, Long> {

    List<Actor> findByDataStatusTrueAndStatus(StoreStatus status);
    Optional<Actor> findByIdAndDataStatusTrueAndStatus(Long id, StoreStatus status);
}

