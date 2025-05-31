package dev.crepe.domain.core.deposit.service;

import dev.crepe.domain.core.deposit.service.impl.TokenDepositServiceImpl;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;

import java.math.BigDecimal;

public interface TokenDepositService{
    String depositToProduct(String userEmail, Long productId, BigDecimal amount);
    void depositSavingBeforeSubscribe(String userEmail, Subscribe subscribe, BigDecimal amount);
}
