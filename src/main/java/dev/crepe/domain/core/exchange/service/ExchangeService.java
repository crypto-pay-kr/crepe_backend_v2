package dev.crepe.domain.core.exchange.service;

import dev.crepe.domain.core.exchange.model.dto.GetExchangeRequest;

public interface ExchangeService {

    void exchangeToToken(String email, GetExchangeRequest request);
    void exchangeToCoin(String email, GetExchangeRequest request);





}
