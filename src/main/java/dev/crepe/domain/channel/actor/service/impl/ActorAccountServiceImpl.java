package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.admin.exception.AlreadyHoldAddressException;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.channel.actor.model.dto.response.BankTokenAccountDto;
import dev.crepe.domain.channel.actor.model.dto.response.GetAllBalanceResponse;
import dev.crepe.domain.channel.actor.service.ActorAccountService;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBankTokenInfoResponse;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeResponseDto;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActorAccountServiceImpl implements ActorAccountService {

    private final AccountService accountService;
    private final BankTokenRepository bankTokenRepository;
    private final SubscribeRepository subscribeRepository;
    private final AccountRepository accountRepository;


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
    public List<BankTokenAccountDto> getMyAccountsSubscription(String email) {

        List<Account> accounts = accountRepository.findByActor_Email(email);

        // bankToken 기준으로 그룹핑
        Map<BankToken, List<Account>> groupedToken = accounts.stream()
                .filter(account -> account.getBankToken() != null)
                .collect(Collectors.groupingBy(Account::getBankToken));

        // 각 BankToken 별로 Subscribe 정보 및 계좌 잔액 수집
        return groupedToken.entrySet().stream()
                .map(entry -> {
                    BankToken token = entry.getKey();
                    List<Account> userAccounts = entry.getValue();

                    List<SubscribeResponseDto> subscribes = subscribeRepository
                            .findAllByUser_EmailAndProduct_BankToken_Id(email, token.getId())
                            .stream()
                            .map(SubscribeResponseDto::from)
                            .collect(Collectors.toList());

                    List<String> balances = userAccounts.stream()
                            .map(account -> account.getBalance().toPlainString())
                            .collect(Collectors.toList());

                    return new BankTokenAccountDto(
                            token.getId(),
                            token.getBank().getId(),
                            token.getName(),
                            balances,
                            token.getCurrency(),
                            subscribes
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTokenBalance(String email, String currency) {
        return accountService.getTokenBalance(email, currency);
    }

    @Override
    public void unRegisterAccount(String email, String currency) {
        accountService.unRegisterAccount(email, currency);

    }

    @Override
    public void holdActorAccount(Account account) {

        if (account.getAddressRegistryStatus() == AddressRegistryStatus.HOLD) {
            throw new AlreadyHoldAddressException(account.getAccountAddress());
        }
        accountService.holdAccount(account);

    }

    @Override
    public GetAllBalanceResponse getAllBalance(String email) {
        List<GetBalanceResponse> balanceList = accountService.getBalanceList(email);
        List<GetBankTokenInfoResponse> bankTokenAccounts = accountService.getBankTokensInfo(email);

        return new GetAllBalanceResponse(balanceList, bankTokenAccounts);
    }


}
