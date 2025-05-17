package dev.crepe.domain.core.subscribe.repository;

import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.SimpleTimeZone;

public interface SubscribeRepository extends JpaRepository<Subscribe,Long> {
    Optional<Subscribe> findByuser_EmailAndProductId(String email, Long productId);
}
