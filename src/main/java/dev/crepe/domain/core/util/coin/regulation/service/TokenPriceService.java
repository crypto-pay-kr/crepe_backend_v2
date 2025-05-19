package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.TokenPrice;

import java.math.BigDecimal;

public interface TokenPriceService {

    void createAndSaveTokenPrice(BankToken bankToken, BigDecimal price);
}
