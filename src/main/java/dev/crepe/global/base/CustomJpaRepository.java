package dev.crepe.global.base;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserEntity로부터 이메일을 받아서 조회하는 공통 JpaRepository
 * @param <T>
 */
public interface CustomJpaRepository<T extends Actor, email extends Long> extends JpaRepository<T, email> {
    Optional<T> findOneByEmail(String email);

    @Override
    <S extends T> S save(S entity);


}
