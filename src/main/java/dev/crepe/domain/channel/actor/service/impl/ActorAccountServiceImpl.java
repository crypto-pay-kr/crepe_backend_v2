package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.channel.actor.service.ActorAccountService;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import dev.crepe.domain.core.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActorAccountServiceImpl implements ActorAccountService {

    private final AccountService accountService;

    @Override
    public void createAccountAddress(GetAddressRequest request, String email) {
        accountService.submitAccountRegistrationRequest(request, email);
    }

    @Override
    public void reRegisterAddress(GetAddressRequest request, String email) {
        accountService.reRegisterAddress(email, request);
    }

    @Override
    public GetAddressResponse getAddressByCurrency(String currency, String email) {
        return accountService.getAddressByCurrency(currency, email);
    }

    @Override
    public List<GetBalanceResponse> getBalanceList(String email) {
        return accountService.getBalanceList(email);
    }

    @Override
    public GetBalanceResponse getBalanceByCurrency(String currency, String email) {
        return accountService.getBalanceByCurrency(email, currency);
    }

    @Override
    public void unRegisterAccount(String email, String currency) {
        accountService.unRegisterAccount(email, currency);
    }

}
