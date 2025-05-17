package dev.crepe.domain.core.account.service.impl;

import dev.crepe.domain.bank.model.entity.Bank;
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
import dev.crepe.domain.core.account.util.GenerateAccountAddress;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final GenerateAccountAddress generateAccountAddress;
    private final AccountRepository accountRepository;
    private final CoinRepository coinRepository;
    private final BankTokenRepository bankTokenRepository;
    private final ActorRepository actorRepository;

    @Override
    @Transactional
    public void createBasicAccounts(Actor actor) {

        //지원하는 모든 코인에 대해서 기본 계좌 생성
        List<Coin> coins = coinRepository.findAll();

        for (Coin coin : coins) {
            Account account = Account.builder()
                    .actor(actor)
                    .coin(coin)
                    .accountAddress(null)
                    .build();
            accountRepository.save(account);
        }
    }

    @Override
    @Transactional
    public void createBasicBankAccounts(Bank bank) {

        //지원하는 모든 코인에 대해서 기본 계좌 생성
        List<Coin> coins = coinRepository.findAll();

        for (Coin coin : coins) {
            Account account = Account.builder()
                    .bank(bank)
                    .coin(coin)
                    .accountAddress(null)
                    .build();
            accountRepository.save(account);
        }
    }

    @Override
    @Transactional
    public void createBankTokenAccount(BankToken bankToken) {
        String accountAddress;
        do {
            accountAddress = generateAccountAddress.generate(bankToken.getBank());
        } while (accountRepository.existsByAccountAddress(accountAddress));

        Account account = Account.builder()
                .bank(bankToken.getBank())
                .bankToken(bankToken)
                .nonAvailableBalance(BigDecimal.ZERO)
                .balance(bankToken.getTotalSupply())
                .addressRegistryStatus(AddressRegistryStatus.REGISTERING)
                .accountAddress(accountAddress)
                .build();

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void updateBankTokenAccount(BankToken bankToken) {
        Account existingAccount = accountRepository.findByBankAndBankToken(bankToken.getBank(), bankToken)
                .orElseThrow(() -> new AccountNotFoundException("해당 BankToken에 연결된 계좌를 찾을 수 없습니다."));

        Account updatedAccount = Account.builder()
                .id(existingAccount.getId()) // 기존 ID 유지
                .bank(existingAccount.getBank())
                .bankToken(existingAccount.getBankToken())
                .nonAvailableBalance(existingAccount.getNonAvailableBalance())
                .balance(existingAccount.getBalance())
                .addressRegistryStatus(existingAccount.getAddressRegistryStatus())
                .accountAddress(existingAccount.getAccountAddress())
                .build();

        accountRepository.save(updatedAccount);
    }

    @Override
    @Transactional
    public void activeBankTokenAccount(BankToken bankToken, TokenHistory tokenHistory) {
        Account account = accountRepository.findByBankAndBankToken(bankToken.getBank(), bankToken)
                .orElseThrow(() -> new AccountNotFoundException("해당 BankToken에 연결된 계좌를 찾을 수 없습니다."));

        Account updatedAccount = Account.builder()
                .id(account.getId())
                .bank(account.getBank())
                .bankToken(bankToken)
                .nonAvailableBalance(account.getNonAvailableBalance())
                .balance(tokenHistory.getTotalSupplyAmount())
                .addressRegistryStatus(AddressRegistryStatus.ACTIVE)
                .accountAddress(account.getAccountAddress())
                .build();

        accountRepository.save(updatedAccount);
    }

    @Override
    public List<GetBalanceResponse> getBalanceList(String email) {

        List<Account> accounts = "BANK".equalsIgnoreCase(SecurityUtil.getRoleByEmail(email))
                ? accountRepository.findByBank_Email(email)
                : accountRepository.findByActor_Email(email);

        boolean hasValidCoinId = accounts.stream().anyMatch(account -> account.getCoin() != null);
        if (!hasValidCoinId) {
            throw new AccountNotFoundException();
        }

        return accounts.stream()
                .filter(account -> account.getCoin() != null)
                .map(account -> GetBalanceResponse.builder()
                        .coinName(account.getCoin().getName())
                        .currency(account.getCoin().getCurrency())
                        .balance(account.getBalance())
                        .build())
                .toList();
    }

    @Override
    public GetBalanceResponse getBalanceByCurrency(String email, String currency) {

        Account account = "BANK".equalsIgnoreCase(SecurityUtil.getRoleByEmail(email))
                ? accountRepository.findByBank_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException("해당 이메일과 통화로 등록된 계좌가 없습니다."))
                : accountRepository.findByActor_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException("해당 이메일과 통화로 등록된 계좌가 없습니다."));

        return GetBalanceResponse.builder()
                .coinName(account.getCoin().getName())
                .currency(account.getCoin().getCurrency())
                .balance(account.getBalance())
                .build();
    }

    @Transactional
    @Override
    public void submitAccountRegistrationRequest(GetAddressRequest request, String email) {

        // 1. 계좌조회
        Account account = "BANK".equalsIgnoreCase(SecurityUtil.getRoleByEmail(email))
                ? accountRepository.findByBank_EmailAndCoin_Currency(email, request.getCurrency())
                .orElseThrow(AccountNotFoundException::new)
                : accountRepository.findByActor_EmailAndCoin_Currency(email, request.getCurrency())
                .orElseThrow(AccountNotFoundException::new);

        // 2. 코인 정보 조회
        Coin coin = coinRepository.findByCurrency(request.getCurrency());

        // 3. 태그 필수 코인일 경우 유효성 검사
        if (coin.isTag() && (request.getTag() == null || request.getTag().isBlank())) {
            throw new TagRequiredException(request.getCurrency());
        }

        // 4. 이미 등록된 주소가 있는 경우 중복 등록 방지
        if (account.getAccountAddress() != null) {
            throw new DuplicateAccountException(request.getCurrency());
        }

        // 5. 주소 및 태그 등록 처리
        account.registerAddress(request.getAddress(), request.getTag());
    }



    @Transactional(readOnly = true)
    @Override
    public GetAddressResponse getAddressByCurrency(String currency, String email) {

        Account account = "BANK".equalsIgnoreCase(SecurityUtil.getRoleByEmail(email))
                ? accountRepository.findByBank_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException(email))
                : accountRepository.findByActor_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException(email));

        Coin coin = coinRepository.findByCurrency(currency);


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

        Account account = "BANK".equalsIgnoreCase(SecurityUtil.getRoleByEmail(email))
                ? accountRepository.findByBank_EmailAndCoin_Currency(email, request.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(request.getCurrency()))
                : accountRepository.findByActor_EmailAndCoin_Currency(email, request.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(request.getCurrency()));

        Coin coin = coinRepository.findByCurrency(request.getCurrency());
        if (coin.isTag() && (request.getTag() == null || request.getTag().isBlank())) {
            throw new TagRequiredException(request.getCurrency());
        }

        account.registerAddress(request.getAddress(), request.getTag());
    }

    @Override
    @Transactional(readOnly = true)
    public String getAccountOwnerName(String email, String currency) {
        Account account = accountRepository.findByBank_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException(email));

        return account.getBank() != null ? account.getBank().getName() : null;
    }


    @Override
    public Account getOrCreateTokenAccount(String email, String tokenCurrency) {
        return accountRepository.findByActor_EmailAndBankToken_Currency(email, tokenCurrency)
                .orElseGet(() -> {
                    BankToken token = bankTokenRepository.findByCurrency(tokenCurrency)
                            .orElseThrow(() -> new IllegalArgumentException("토큰 없음"));
                    Actor actor = actorRepository.findByEmail(email)
                            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

                    return accountRepository.save(Account.builder()
                            .actor(actor)
                            .bankToken(token)
                            .balance(BigDecimal.ZERO)
                            .build());
                });
    }

}

