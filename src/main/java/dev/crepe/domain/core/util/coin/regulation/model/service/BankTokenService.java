package dev.crepe.domain.core.util.coin.regulation.model.service;

import java.math.BigDecimal;

public interface BankTokenService {

    BigDecimal getTokenPrice(String fromCurrency);

}
