package dev.crepe.domain.channel.actor.repository;


import dev.crepe.domain.channel.actor.model.RoleCountProjection;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.store.model.StoreStatus;
import dev.crepe.domain.core.product.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Long> {

    Optional<Actor> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    boolean existsByPhoneNum(String phoneNum);

    @Query("SELECT a.role AS role, COUNT(a) AS count FROM Actor a GROUP BY a.role")
    List<RoleCountProjection> countActorsByRole();


}
