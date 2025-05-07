package dev.crepe.domain.channel.actor.repository;


import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.store.model.StoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Long> {

    Optional<Actor> findByEmail(String email);

    // store
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    boolean existsByPhoneNum(String phoneNum);
    List<Actor> findByDataStatusTrueAndStatus(StoreStatus status);
    Optional<Actor> findByIdAndDataStatusTrueAndStatus(Long id, StoreStatus status);
}
