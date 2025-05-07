package dev.crepe.domain.core.account.service.impl;

import dev.crepe.domain.channel.actor.store.model.entity.Store;
import dev.crepe.domain.channel.actor.store.repository.StoreRepository;
import dev.crepe.domain.channel.actor.user.model.entity.User;
import dev.crepe.domain.channel.actor.user.repository.UserRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CoinRepository coinRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Override
    public List<GetBalanceResponse> getBalanceList(String email) {
        List<Account> accounts = getAccountsByEmail(email);

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
        return getAccountsByEmail(email).stream()
                .filter(account -> account.getCoin().getCurrency().equalsIgnoreCase(currency))
                .map(account -> GetBalanceResponse.builder()
                        .coinName(account.getCoin().getName())
                        .currency(account.getCoin().getCurrency())
                        .balance(account.getBalance())
                        .build())
                .findAny()
                .orElse(null);
    }

    private List<Account> getAccountsByEmail(String email) {
        List<Account> userAccounts = accountRepository.findByUser_Email(email);
        if (!userAccounts.isEmpty()) {
            return userAccounts;
        }
        return accountRepository.findByStore_Email(email);
    }

    @Transactional
    @Override
    public void submitAccountRegistrationRequest(GetAddressRequest request, String email) {
        Coin coin = coinRepository.findByCurrency(request.getCurrency());

        if (coin.isTag() && (request.getTag() == null || request.getTag().isBlank())) {
            throw new TagRequiredException(request.getCurrency());
        }

        if (existsByEmailAndCurrency(email, request.getCurrency())) {
            throw new DuplicateAccountException(request.getCurrency());
        }

        Account.AccountBuilder builder = Account.builder()
                .coin(coin)
                .accountAddress(request.getAddress())
                .tag(request.getTag())
                .addressRegistryStatus(AddressRegistryStatus.REGISTERING);

        userRepository.findByEmail(email).ifPresent(builder::user);
        storeRepository.findByEmail(email).ifPresent(builder::store);

        accountRepository.save(builder.build());
    }

    @Transactional(readOnly = true)
    @Override
    public GetAddressResponse getAddressByCurrency(String currency, String email) {
        Coin coin = coinRepository.findByCurrency(currency);

        Account account = findAccountByEmailAndCurrency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException(currency));

        GetAddressResponse.GetAddressResponseBuilder responseBuilder = GetAddressResponse.builder()
                .currency(coin.getCurrency())
                .address(account.getAccountAddress())
                .addressRegistryStatus(account.getAddressRegistryStatus().name());

        if (coin.isTag()) {
            responseBuilder.tag(account.getTag());
        }

        return responseBuilder.build();
    }

    @Transactional
    @Override
    public void reRegisterAddress(String email, GetAddressRequest request) {
        Account account = findAccountByEmailAndCurrency(email, request.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(request.getCurrency()));

        Account renewAccount = Account.builder()
                .id(account.getId())
                .user(account.getUser())
                .store(account.getStore())
                .coin(account.getCoin())
                .balance(account.getBalance())
                .accountAddress(request.getAddress())
                .tag(request.getTag())
                .addressRegistryStatus(AddressRegistryStatus.REGISTERING)
                .build();

        accountRepository.save(renewAccount);
    }

    // 중복 확인 함수
    private boolean existsByEmailAndCurrency(String email, String currency) {
        return accountRepository.findByUser_EmailAndCoin_Currency(email, currency).isPresent()
                || accountRepository.findByStore_EmailAndCoin_Currency(email, currency).isPresent();
    }

    // 유저 or 스토어 계정 조회 통합 함수
    private Optional<Account> findAccountByEmailAndCurrency(String email, String currency) {
        return accountRepository.findByUser_EmailAndCoin_Currency(email, currency)
                .or(() -> accountRepository.findByStore_EmailAndCoin_Currency(email, currency));
    }
}