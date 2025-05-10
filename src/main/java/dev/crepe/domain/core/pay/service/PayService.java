package dev.crepe.domain.core.pay.service;

import dev.crepe.domain.channel.market.order.model.entity.Order;



public interface PayService {

    void payForOrder(Order order );

}
