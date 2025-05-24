package dev.crepe.domain.channel.actor.repository;


import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Long> {

    Optional<Actor> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    boolean existsByPhoneNum(String phoneNum);
    Page<Actor> findByRole(UserRole role, Pageable pageable);
    List<Actor> findByDataStatusTrue();
}
