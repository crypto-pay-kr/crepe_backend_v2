package dev.crepe.domain.core.util.history.pay.service.impl;


import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.util.history.pay.execption.PayHistoryNotFoundException;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import dev.crepe.domain.core.util.history.pay.repostiory.PayHistoryRepository;
import dev.crepe.domain.core.util.history.pay.service.PayHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PayHistoryServiceImpl implements PayHistoryService {

    private final PayHistoryRepository payHistoryRepository;

    @Override
    public PayHistory getPayHistoryByOrderId(String orderId) {
        return payHistoryRepository.findByOrderId(orderId)
                .orElseThrow(PayHistoryNotFoundException::new);
    }

}
