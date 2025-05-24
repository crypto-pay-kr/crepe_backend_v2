package dev.crepe.domain.core.subscribe.repository;


import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import org.springframework.data.jpa.repository.JpaRepository;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.product.model.entity.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.SimpleTimeZone;

public interface SubscribeRepository extends JpaRepository<Subscribe,Long> {
    Optional<Subscribe> findByuser_EmailAndProductId(String email, Long productId);

    List<Subscribe> findAllByProduct_TypeAndExpiredDateBeforeAndStatus(
            BankProductType type,
            LocalDateTime expiredBefore,
            SubscribeStatus status
    );

    List<Subscribe> findAllByUser_EmailAndProduct_BankToken_Id(String email, Long bankTokenId);


    List<Subscribe> findAllByUser_Email(String email);

    List<Subscribe> findByUser_Email(String email);
    boolean existsByUserAndProduct(Actor user, Product product);

    Integer countByProductIdAndStatus(Long productId, SubscribeStatus subscribeStatus);
}
