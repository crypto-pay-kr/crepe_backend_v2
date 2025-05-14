package dev.crepe.domain.core.deposit.service;

import dev.crepe.domain.core.deposit.service.impl.TokenDepositServiceImpl;

import java.math.BigDecimal;

public interface TokenDepositService{
    String depositToProduct(Long actorId, Long productId, BigDecimal amount);
}
