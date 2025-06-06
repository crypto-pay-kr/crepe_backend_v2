package dev.crepe.domain.channel.market.order.service;


import dev.crepe.domain.channel.market.order.model.dto.request.CreateOrderRequest;
import dev.crepe.domain.channel.market.order.model.dto.response.CreateOrderResponse;
import dev.crepe.domain.channel.market.order.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface OrderService {

    // 특정 사용자의 주문 목록 조회
    List<CreateOrderResponse> getCustomerOrderList(String userEmail);

    // 주문 상세 조회
    CreateOrderResponse getOrderDetails(String orderId, String userEmail);

    // 주문 생성
    Map<String, String> createOrder(CreateOrderRequest request, String userEmail);

    // 관리자 주문 조회
    Page<Order> getOrdersByUserId(Long userId, Pageable pageable);
    Page<Order> getOrdersByStoreId(Long storeId, Pageable pageable);

    // 유저 주문 조회
    Page<Order> getOrdersByUserEmail(String userEmail, Pageable pageable);

    List<String> getAvailableCurrency(String userEmail, Long storeId);

}
