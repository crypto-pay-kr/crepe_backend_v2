package dev.crepe.domain.core.util.history.pay.service;

import dev.crepe.domain.admin.dto.response.GetPayHistoryResponse;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayHistoryService {

    PayHistory getPayHistoryByOrderId(String orderId);
}
