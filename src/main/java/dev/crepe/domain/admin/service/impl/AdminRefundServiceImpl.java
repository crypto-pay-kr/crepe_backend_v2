package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.service.AdminRefundService;
import dev.crepe.domain.core.pay.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRefundServiceImpl implements AdminRefundService {

    private final PayService payService;

    @Override
    @Transactional
    public void approveRefund(Long payId, Long id) {
        payService.refundForOrder(payId, id);
    }
}
