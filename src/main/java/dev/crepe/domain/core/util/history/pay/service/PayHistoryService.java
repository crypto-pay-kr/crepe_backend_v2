package dev.crepe.domain.core.util.history.pay.service;

import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;

public interface PayHistoryService {


    PayHistory getPayHistoryByOrderId(String orderId);
}
