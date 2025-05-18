package dev.crepe.domain.core.subscribe.repository;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscribeRepository  extends JpaRepository<Subscribe, Long> {

    boolean existsByUserAndProduct(Actor user, Product product);
}
