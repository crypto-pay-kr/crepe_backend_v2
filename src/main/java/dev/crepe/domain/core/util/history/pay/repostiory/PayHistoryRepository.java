package dev.crepe.domain.core.util.history.pay.repostiory;

import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayHistoryRepository extends JpaRepository<PayHistory, Long> {

    Optional<PayHistory> findByOrder(Order order);


}
