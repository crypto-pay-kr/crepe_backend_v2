package dev.crepe.domain.channel.actor.store.repository;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;




public interface StoreRepository extends JpaRepository<Actor, Long> {

}

