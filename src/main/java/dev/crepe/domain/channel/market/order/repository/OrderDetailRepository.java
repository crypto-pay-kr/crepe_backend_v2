package dev.crepe.domain.channel.market.order.repository;

import dev.crepe.domain.channel.market.order.model.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {

    List<OrderDetail> findByOrderId(String orderId);


}
