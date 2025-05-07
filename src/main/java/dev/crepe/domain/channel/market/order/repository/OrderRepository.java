package dev.crepe.domain.channel.market.order.repository;

import dev.crepe.domain.channel.market.order.model.OrderStatus;
import dev.crepe.domain.channel.market.order.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByStoreId(Long storeId);

    List<Order> findByUserId(Long userId);

    List<Order> findByStoreIdAndStatus(Long storeId, OrderStatus status);

    Optional<Order> findByIdAndStoreId(String orderId, Long storeId);

}
