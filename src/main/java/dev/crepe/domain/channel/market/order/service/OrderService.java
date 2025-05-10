package dev.crepe.domain.channel.market.order.service;


import dev.crepe.domain.channel.market.order.model.dto.request.CreateOrderRequest;
import dev.crepe.domain.channel.market.order.model.dto.response.CreateOrderResponse;

import java.util.List;

public interface OrderService {

    // 특정 사용자의 주문 목록 조회
    List<CreateOrderResponse> getCustomerOrderList(String userEmail);

    // 주문 상세 조회
    CreateOrderResponse getOrderDetails(String orderId, String userEmail);

    // 주문 생성
    String createOrder(CreateOrderRequest request, String userEmail);


}
