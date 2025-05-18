package dev.crepe.domain.core.subscribe.repository;

import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
