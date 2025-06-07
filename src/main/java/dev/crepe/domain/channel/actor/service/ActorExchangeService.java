package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.core.exchange.model.dto.request.CreateExchangeRequest;
import dev.crepe.domain.core.util.coin.regulation.model.dto.request.TokenInfoResponse;

public interface ActorExchangeService {

    void requestExchangeToCoin(String email, CreateExchangeRequest request, String traceId);
    void requestExchangeToToken(String email, CreateExchangeRequest request,String traceId);
    TokenInfoResponse getBankTokenInfo(String currency);
}
