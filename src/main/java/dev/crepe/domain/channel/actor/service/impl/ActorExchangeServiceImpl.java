package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.channel.actor.service.ActorExchangeService;
import dev.crepe.domain.core.exchange.model.dto.request.GetExchangeRequest;
import dev.crepe.domain.core.exchange.service.ExchangeService;
import dev.crepe.domain.core.util.coin.regulation.model.dto.request.TokenInfoResponse;
import dev.crepe.domain.core.util.coin.regulation.service.BankTokenInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ActorExchangeServiceImpl implements ActorExchangeService {

    private final ExchangeService exchangeService;
    private final BankTokenInfoService bankTokenInfoService;

    @Override
    public void RequestExchangeToCoin(String email, GetExchangeRequest getExchangeRequest) {
        exchangeService.exchangeToCoin(email, getExchangeRequest);
    }

    @Override
    public void RequestExchangeToToken(String email, GetExchangeRequest getExchangeRequest) {
        exchangeService.exchangeToToken(email, getExchangeRequest);
    }

    @Override
    public TokenInfoResponse GetBankTokenInfo(String currency) {
        return bankTokenInfoService.getTokenInfo(currency);
    }
}