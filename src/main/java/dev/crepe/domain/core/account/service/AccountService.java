package dev.crepe.domain.core.account.service;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;

import java.util.List;

public interface AccountService {


    void createBasicAccounts(Actor actor);
    void createBasicBankAccounts(Bank bank);

    void createBankTokenAccount(BankToken bankToken);

    List<GetBalanceResponse> getBalanceList(String userEmail);
    GetBalanceResponse getBalanceByCurrency(String userEmail, String currency);

    void submitAccountRegistrationRequest(GetAddressRequest request, String email);
    GetAddressResponse getAddressByCurrency(String currency, String email);
    void reRegisterAddress(String email, GetAddressRequest request);
}
