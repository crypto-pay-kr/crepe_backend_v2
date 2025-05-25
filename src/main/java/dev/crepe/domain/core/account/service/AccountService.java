package dev.crepe.domain.core.account.service;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBankTokenInfoResponse;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountService {


    void createBasicAccounts(Actor actor);
    void createBasicBankAccounts(Bank bank);

    void createBankTokenAccount(BankToken bankToken);

    void updateBankTokenAccount(BankToken bankToken);

    void activeBankTokenAccount(BankToken bankToken, TokenHistory tokenHistory);

    List<GetBalanceResponse> getBalanceList(String userEmail);
    GetBalanceResponse getBalanceByCurrency(String userEmail, String currency);

    Account getAccountById(Long accountId);

    void submitAccountRegistrationRequest(GetAddressRequest request, String email);
    GetAddressResponse getAddressByCurrency(String currency, String email);
    void reRegisterAddress(String email, GetAddressRequest request);

    String getAccountOwnerName(String email, String currency);
    List<Account> getAccountsByBankEmail(String bankEmail);

    Account findBankTokenAccount(Long bankId, BankToken bankToken);

    Optional<Account> findByBankAndBankTokenAndAddressRegistryStatus(Bank bank, BankToken bankToken, AddressRegistryStatus status);

    void findActiveAccountByBankEmailAndCurrency(String bankEmail, String currency);

    Account getOrCreateTokenAccount(String email, String tokenCurrency);

    BigDecimal getTokenBalance(String email, String currency);


    void unRegisterAccount(String email, String currency);

    void holdAccount(Account account);

    void validateAccountNotHold(Account account);

    List<GetBankTokenInfoResponse> getBankTokensInfo(String email);

}
