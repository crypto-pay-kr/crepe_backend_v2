package dev.crepe.domain.core.account.service.impl;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.exception.DuplicateAccountException;
import dev.crepe.domain.core.account.exception.TagRequiredException;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CoinRepository coinRepository;
    private final ActorRepository actorRepository;

    @Override
    public List<GetBalanceResponse> getBalanceList(String email) {
        List<Account> accounts = accountRepository.findByActor_Email(email);

        return accounts.stream()
                .map(account -> GetBalanceResponse.builder()
                        .coinName(account.getCoin().getName())
                        .currency(account.getCoin().getCurrency())
                        .balance(account.getBalance())
                        .build())
                .toList();
    }

    @Override
    public GetBalanceResponse getBalanceByCurrency(String email, String currency) {
        return accountRepository.findByActor_Email(email).stream()
                .filter(account -> account.getCoin().getCurrency().equalsIgnoreCase(currency))
                .map(account -> GetBalanceResponse.builder()
                        .coinName(account.getCoin().getName())
                        .currency(account.getCoin().getCurrency())
                        .balance(account.getBalance())
                        .build())
                .findAny()
                .orElse(null);
    }

    @Transactional
    @Override
    public void submitAccountRegistrationRequest(GetAddressRequest request, String email) {
        Actor actor = actorRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 이메일입니다: " + email));

        Coin coin = coinRepository.findByCurrency(request.getCurrency());

        if (coin.isTag() && (request.getTag() == null || request.getTag().isBlank())) {
            throw new TagRequiredException(request.getCurrency());
        }

        if (accountRepository.findByActor_EmailAndCoin_Currency(email, request.getCurrency()).isPresent()) {
            throw new DuplicateAccountException(request.getCurrency());
        }

        Account account = Account.builder()
                .coin(coin)
                .accountAddress(request.getAddress())
                .tag(request.getTag())
                .addressRegistryStatus(AddressRegistryStatus.REGISTERING)
                .actor(actor)
                .build();

        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    @Override
    public GetAddressResponse getAddressByCurrency(String currency, String email) {
        Coin coin = coinRepository.findByCurrency(currency);

        Account account = accountRepository.findByActor_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException(email));

        GetAddressResponse.GetAddressResponseBuilder builder = GetAddressResponse.builder()
                .currency(coin.getCurrency())
                .address(account.getAccountAddress())
                .addressRegistryStatus(account.getAddressRegistryStatus().name());

        if (coin.isTag()) {
            builder.tag(account.getTag());
        }

        return builder.build();
    }

    @Transactional
    @Override
    public void reRegisterAddress(String email, GetAddressRequest request) {
        Account account = accountRepository.findByActor_EmailAndCoin_Currency(email, request.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(request.getCurrency()));

        Account renewAccount = Account.builder()
                .id(account.getId())
                .actor(account.getActor())
                .coin(account.getCoin())
                .balance(account.getBalance())
                .accountAddress(request.getAddress())
                .tag(request.getTag())
                .addressRegistryStatus(AddressRegistryStatus.REGISTERING)
                .build();

        accountRepository.save(renewAccount);
    }
}