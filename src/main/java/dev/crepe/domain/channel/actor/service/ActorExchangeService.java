package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.core.exchange.model.dto.request.GetExchangeRequest;
import dev.crepe.domain.core.util.coin.regulation.model.dto.request.TokenInfoResponse;

public interface ActorExchangeService {

    void RequestExchangeToCoin(String email, GetExchangeRequest request);
    void RequestExchangeToToken(String email, GetExchangeRequest request);
    TokenInfoResponse GetBankTokenInfo(String currency);
}
