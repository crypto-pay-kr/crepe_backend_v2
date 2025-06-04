package dev.crepe.domain.core.util.history.pay.service.impl;

import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import dev.crepe.domain.core.util.history.pay.repostiory.PayHistoryRepository;
import dev.crepe.domain.core.util.history.pay.service.PayHistoryService;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PayHistoryServiceImpl implements PayHistoryService {

    private final ExceptionDbService exceptionDbService;
    private final PayHistoryRepository payHistoryRepository;

    @Override
    public PayHistory getPayHistoryByOrderId(String orderId) {
        return payHistoryRepository.findByOrderId(orderId)
                .orElseThrow(() -> exceptionDbService.getException("PAY_HISTORY_001"));
    }


}
