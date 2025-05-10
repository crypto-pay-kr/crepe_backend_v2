package dev.crepe.domain.channel.actor.store.service;

import dev.crepe.domain.channel.market.order.model.entity.Order;



public interface StoreDepositService {

    void userWithdrawForOrder(Order order );
    void pendingStoreDepositForOrder(Order order, Long storeId);

}
