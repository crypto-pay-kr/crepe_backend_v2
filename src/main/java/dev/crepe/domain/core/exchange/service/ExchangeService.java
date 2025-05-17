package dev.crepe.domain.core.exchange.service;

import dev.crepe.domain.core.exchange.model.dto.request.CreateExchangeRequest;

public interface ExchangeService {

    void exchangeToToken(String email, CreateExchangeRequest request);
    void exchangeToCoin(String email, CreateExchangeRequest request);





}
