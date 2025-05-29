package dev.crepe.domain.channel.actor.store.service;

import dev.crepe.domain.channel.actor.store.model.dto.request.StoreOrderActionRequest;
import dev.crepe.domain.channel.actor.store.model.dto.response.StoreOrderManageResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.StoreOrderResponse;

import java.util.List;

public interface StoreOrderService {


    List<StoreOrderResponse> getAllList(Long storeId);

    StoreOrderManageResponse acceptOrder(String orderId, Long storeId, StoreOrderActionRequest request);

    StoreOrderManageResponse refuseOrder(String orderId, Long storeId, StoreOrderActionRequest request);

    StoreOrderManageResponse completeOrder(String orderId, Long storeId);
}
