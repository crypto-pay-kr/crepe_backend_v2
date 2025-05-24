package dev.crepe.domain.core.account.service.impl;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.exception.AccountOnHoldException;
import dev.crepe.domain.core.account.exception.DuplicateAccountException;
import dev.crepe.domain.core.account.exception.TagRequiredException;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBankTokenInfoResponse;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.account.util.GenerateAccountAddress;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final GenerateAccountAddress generateAccountAddress;
    private final AccountRepository accountRepository;
    private final CoinRepository coinRepository;
    private final BankTokenRepository bankTokenRepository;
    private final ActorRepository actorRepository;
    private final SubscribeRepository subscribeRepository;

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
                .balance(BigDecimal.ZERO)
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

        validateAccountNotHold(existingAccount);

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
                        .coinImageUrl(account.getCoin().getCoinImage())
                        .coinName(account.getCoin().getName())
                        .currency(account.getCoin().getCurrency())
                        .balance(account.getBalance())
                        .build())
                .toList();
    }

    @Override
    public List<GetBankTokenInfoResponse> getBankTokensInfo(String email) {
        // 1. 전체 BankToken과 연결된 모든 계좌 조회
        List<Account> allTokenAccounts = accountRepository.findByBankTokenIdIsNotNullAndBankIdIsNotNull();

        // 2. 유저의 BankToken 계좌 조회
        List<Account> userTokenAccounts = accountRepository.findByActor_EmailAndBankTokenIdIsNotNull(email);

        // 3. 유저 계좌 정보를 Map으로 변환 (bankTokenId → balance)
        Map<Long, BigDecimal> userBalances = userTokenAccounts.stream()
                .collect(Collectors.toMap(
                        acc -> acc.getBankToken().getId(),
                        Account::getBalance
                ));

        // 4. 전체 토큰 계좌 정보를 Map으로 변환 (bankTokenId → Account)
        Map<Long, Account> tokenAccountMap = allTokenAccounts.stream()
                .collect(Collectors.toMap(
                        acc -> acc.getBankToken().getId(),
                        acc -> acc
                ));

        // 5. 유저가 가입한 상품 정보 조회
        List<Subscribe> userSubscribes = subscribeRepository.findByUser_Email(email);
        Map<Long, List<GetBankTokenInfoResponse.GetProductResponse>> subscribedProducts = userSubscribes.stream()
                .collect(Collectors.groupingBy(
                        sub -> sub.getProduct().getBankToken().getId(),
                        Collectors.mapping(sub -> GetBankTokenInfoResponse.GetProductResponse.builder()
                                .subscribeId(sub.getId())
                                .name(sub.getProduct().getProductName())
                                .balance(sub.getBalance())
                                .build(), Collectors.toList())
                ));

        // 5. 결과 생성
        return tokenAccountMap.entrySet().stream()
                .map(entry -> {
                    Long tokenId = entry.getKey();
                    Account account = entry.getValue();
                    String name = account.getBankToken().getName();
                    BigDecimal balance = userBalances.getOrDefault(tokenId, BigDecimal.ZERO);
                    List<GetBankTokenInfoResponse.GetProductResponse> product = subscribedProducts.get(tokenId);

                    return GetBankTokenInfoResponse.builder()
                            .bankImageUrl(account.getBankToken().getBank().getImageUrl())
                            .currency(account.getBankToken().getCurrency())
                            .name(name)
                            .balance(balance)
                            .product(product)
                            .build();
                })
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

    @Override
    @Transactional(readOnly = true)
    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for ID: " + accountId));
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

        // HOLD 상태 계좌 확인
        validateAccountNotHold(account);

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

        // HOLD 상태 계좌 확인
        validateAccountNotHold(account);

        Coin coin = coinRepository.findByCurrency(request.getCurrency());
        if (coin.isTag() && (request.getTag() == null || request.getTag().isBlank())) {
            throw new TagRequiredException(request.getCurrency());
        }

        // 등록된 계좌가 있을 경우 변경 시 해지 후 등록 하도록 상태 변경
        if(account.getAddressRegistryStatus()==AddressRegistryStatus.REGISTERING) {
            account.registerAddress(request.getAddress(), request.getTag());
        }else if(account.getAddressRegistryStatus()==AddressRegistryStatus.NOT_REGISTERED){
            throw new AccountNotFoundException("해당 계좌는 미등록 상태입니다.");
        } else{
            account.reRegisterAddress(request.getAddress(), request.getTag());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getAccountOwnerName(String email, String currency) {
        Account account = accountRepository.findByBank_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException(email));

        return account.getBank() != null ? account.getBank().getName() : null;
    }

    @Override
    public List<Account> getAccountsByBankEmail(String bankEmail) {
        return accountRepository.findByBank_Email(bankEmail);
    }




    @Override
    public Account findBankTokenAccount(Long bankId, BankToken bankToken) {
        return accountRepository.findByBankIdAndBankTokenAndActorIsNull(bankId, bankToken)
                .orElseThrow(() -> new AccountNotFoundException("은행의 BankToken 계좌를 찾을 수 없습니다."));
    }

    @Override
    public Optional<Account> findByBankAndBankTokenAndAddressRegistryStatus(Bank bank, BankToken bankToken, AddressRegistryStatus status) {
        return accountRepository.findByBankAndBankTokenAndAddressRegistryStatus(bank, bankToken, status);
    }

    @Override
    public void findActiveAccountByBankEmailAndCurrency(String bankEmail, String currency) {
        accountRepository.findByBank_EmailAndCoin_CurrencyAndAddressRegistryStatus(
                bankEmail,
                currency,
                AddressRegistryStatus.ACTIVE
        ).orElseThrow(() -> new AccountNotFoundException(bankEmail));
    }


    @Override
    public Account getOrCreateTokenAccount(String email, String tokenCurrency) {

        // 계좌 조회
        Optional<Account> existingAccount = accountRepository.findByActor_EmailAndBankToken_Currency(email, tokenCurrency);

        // HOLD 상태 계좌 확인
        existingAccount.ifPresent(this::validateAccountNotHold);

        // 계좌가 없으면 새로 생성
        return existingAccount.orElseGet(() -> {
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


    @Override
    public BigDecimal getTokenBalance(String email, String currency) {
        return accountRepository.findByActor_EmailAndBankToken_Currency(email, currency).stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    @Override
    public void unRegisterAccount(String email, String currency) {
        Account account = "BANK".equalsIgnoreCase(SecurityUtil.getRoleByEmail(email))
                ? accountRepository.findByBank_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException(currency))
                : accountRepository.findByActor_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException(currency));

        // HOLD 상태 계좌 확인
        validateAccountNotHold(account);

        //계좌가 활성 상태이거나 해지 후 등록 중인 상태일 때만 해지 상태로 변경
        if(account.getAddressRegistryStatus()!=AddressRegistryStatus.ACTIVE&&
                account.getAddressRegistryStatus()!=AddressRegistryStatus.UNREGISTERED_AND_REGISTERING) {
            throw new AccountNotFoundException("해당 계좌는 활성화 상태가 아닙니다.");
        }
        account.unRegisterAddress();
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void holdAccount(Account account) {

        account.adminHoldAddress();
        accountRepository.save(account);

    }

    // 정지상태 계좌 확인
    @Override
    public void validateAccountNotHold(Account account) {
        if (account.getAddressRegistryStatus() == AddressRegistryStatus.HOLD) {
            throw new AccountOnHoldException();
        }
    }
}

