package dev.crepe.domain.core.account;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.impl.AccountServiceImpl;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.global.error.exception.CustomException;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CoinRepository coinRepository;
    @Mock
    private ActorRepository actorRepository;


    @InjectMocks
    private AccountServiceImpl accountService;


    @Mock
    private ExceptionDbService exceptionDbService;

    @Test
    @DisplayName("Actor에 대한 기본 계좌 생성 테스트")
    void createBasicAccounts_ForActor() {
        // given
        Actor actor = Actor.builder()
                .id(1L)
                .name("Test User")
                .email("user@example.com")
                .password("password")
                .role(UserRole.USER)
                .build();

        List<Coin> coins = Arrays.asList(
                createCoin(1L, "Bitcoin", "BTC", false),
                createCoin(2L, "Ethereum", "ETH", false)
        );

        when(coinRepository.findAll()).thenReturn(coins);
        when(actorRepository.findByEmail(actor.getEmail())).thenReturn(Optional.of(actor));
        // when
        accountService.createBasicAccounts(actor.getEmail());

        // then
        verify(coinRepository).findAll();
        verify(actorRepository).findByEmail(actor.getEmail());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("Bank에 대한 기본 계좌 생성 테스트")
    void createBasicAccounts_ForBank() {
        // given
        Bank bank = Bank.builder()
                .email("bank@example.com")
                .build();

        List<Coin> coins = Arrays.asList(
                createCoin(1L, "Bitcoin", "BTC", false),
                createCoin(2L, "Ethereum", "ETH", false)
        );

        when(coinRepository.findAll()).thenReturn(coins);

        // when
        accountService.createBasicBankAccounts(bank);

        // then
        verify(coinRepository).findAll();
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("Actor 계정에 대한 잔액 목록 조회 테스트")
    void getBalanceList_ForActor() {
        // given
        String email = "user@example.com";
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(() -> SecurityUtil.getRoleByEmail(email))
                    .thenReturn("ACTOR");
            
            List<Account> accounts = Arrays.asList(
                    createAccount(1L, "BTC", new BigDecimal("1.5")),
                    createAccount(2L, "ETH", new BigDecimal("10.0"))
            );

            when(accountRepository.findByActor_Email(email)).thenReturn(accounts);

            // when
            List<GetBalanceResponse> result = accountService.getBalanceList(email);

            // then
            assertEquals(2, result.size());
            assertEquals("BTC", result.get(0).getCurrency());
            assertEquals(new BigDecimal("1.5"), result.get(0).getBalance());
            assertEquals("ETH", result.get(1).getCurrency());
            assertEquals(new BigDecimal("10.0"), result.get(1).getBalance());
        }
    }

    @Test
    @DisplayName("Bank 계정에 대한 잔액 목록 조회 테스트")
    void getBalanceList_ForBank() {
        // given
        String email = "bank@example.com";
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(() -> SecurityUtil.getRoleByEmail(email))
                    .thenReturn("BANK");
            
            List<Account> accounts = Arrays.asList(
                    createAccount(1L, "BTC", new BigDecimal("100.5")),
                    createAccount(2L, "ETH", new BigDecimal("500.0"))
            );

            when(accountRepository.findByBank_Email(email)).thenReturn(accounts);

            // when
            List<GetBalanceResponse> result = accountService.getBalanceList(email);

            // then
            assertEquals(2, result.size());
            assertEquals("BTC", result.get(0).getCurrency());
            assertEquals(new BigDecimal("100.5"), result.get(0).getBalance());
            assertEquals("ETH", result.get(1).getCurrency());
            assertEquals(new BigDecimal("500.0"), result.get(1).getBalance());
        }
    }

    @Test
    @DisplayName("특정 통화에 대한 잔액 조회 테스트 - Actor")
    void getBalanceByCurrency_ForActor() {
        // given
        String email = "user@example.com";
        String currency = "BTC";
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(() -> SecurityUtil.getRoleByEmail(email))
                    .thenReturn("ACTOR");
            
            Account account = createAccount(1L, currency, new BigDecimal("1.5"));
            when(accountRepository.findByActor_EmailAndCoin_Currency(email, currency))
                    .thenReturn(Optional.of(account));

            // when
            GetBalanceResponse result = accountService.getBalanceByCurrency(email, currency);

            // then
            assertNotNull(result);
            assertEquals(currency, result.getCurrency());
            assertEquals(new BigDecimal("1.5"), result.getBalance());
        }
    }

    @Test
    @DisplayName("특정 통화에 대한 잔액 조회 테스트 - Bank")
    void getBalanceByCurrency_ForBank() {
        // given
        String email = "bank@example.com";
        String currency = "ETH";
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(() -> SecurityUtil.getRoleByEmail(email))
                    .thenReturn("BANK");
            
            Account account = createAccount(2L, currency, new BigDecimal("500.0"));
            when(accountRepository.findByBank_EmailAndCoin_Currency(email, currency))
                    .thenReturn(Optional.of(account));

            // when
            GetBalanceResponse result = accountService.getBalanceByCurrency(email, currency);

            // then
            assertNotNull(result);
            assertEquals(currency, result.getCurrency());
            assertEquals(new BigDecimal("500.0"), result.getBalance());
        }
    }

    @Test
    @DisplayName("계좌 등록 요청 성공 테스트")
    void submitAccountRegistrationRequest_Success() {
        // given
        String email = "user@example.com";
        GetAddressRequest request = new GetAddressRequest("BTC", "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", null);
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(() -> SecurityUtil.getRoleByEmail(email))
                    .thenReturn("ACTOR");
            
            Account account = Account.builder()
                    .coin(createCoin(1L, "Bitcoin", "BTC", false))
                    .build();
            
            when(accountRepository.findByActor_EmailAndCoin_Currency(email, "BTC"))
                    .thenReturn(Optional.of(account));
            when(coinRepository.findByCurrency("BTC"))
                    .thenReturn(createCoin(1L, "Bitcoin", "BTC", false));

            // when
            accountService.submitAccountRegistrationRequest(request, email);

            // then
            assertEquals("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", account.getAccountAddress());
            assertEquals(AddressRegistryStatus.REGISTERING, account.getAddressRegistryStatus());
        }
    }

    @Test
    @DisplayName("태그가 필요한 코인에 태그 없이 계좌 등록 요청 시 예외 발생")
    void submitAccountRegistrationRequest_TagRequired() {
        // given
        String email = "user@example.com";
        GetAddressRequest request = new GetAddressRequest("XRP", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", null);
        // 태그 정보 누락
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(() -> SecurityUtil.getRoleByEmail(email))
                    .thenReturn("ACTOR");
            
            Account account = Account.builder()
                    .coin(createCoin(3L, "Ripple", "XRP", true))
                    .build();
            
            when(accountRepository.findByActor_EmailAndCoin_Currency(email, "XRP"))
                    .thenReturn(Optional.of(account));
            when(coinRepository.findByCurrency("XRP"))
                    .thenReturn(createCoin(3L, "Ripple", "XRP", true));
            when(exceptionDbService.getException("ADDRESS_005"))
                    .thenReturn(new CustomException("ADDRESS_005", null, null));

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    accountService.submitAccountRegistrationRequest(request, email));
            assertEquals("ADDRESS_005", exception.getCode());
        }
    }

    @Test
    @DisplayName("이미 등록된 계좌에 다시 등록 요청 시 예외 발생")
    void submitAccountRegistrationRequest_AlreadyRegistered() {
        // given
        String email = "user@example.com";
        GetAddressRequest request = new GetAddressRequest("BTC", "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", null);
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(() -> SecurityUtil.getRoleByEmail(email))
                    .thenReturn("ACTOR");
            
            Account account = Account.builder()
                    .coin(createCoin(1L, "Bitcoin", "BTC", false))
                    .accountAddress("existing_address")
                    .build();
            
            when(accountRepository.findByActor_EmailAndCoin_Currency(email, "BTC"))
                    .thenReturn(Optional.of(account));
            when(coinRepository.findByCurrency("BTC"))
                    .thenReturn(createCoin(1L, "Bitcoin", "BTC", false));
            when(exceptionDbService.getException("ADDRESS_002"))
                    .thenReturn(new CustomException("ADDRESS_002", null, null));

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    accountService.submitAccountRegistrationRequest(request, email));
            assertEquals("ADDRESS_002", exception.getCode());
        }
    }

    @Test
    @DisplayName("주소 정보 조회 테스트")
    void getAddressByCurrency() {
        // given
        String email = "user@example.com";
        String currency = "BTC";
        
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(() -> SecurityUtil.getRoleByEmail(email))
                    .thenReturn("ACTOR");
            
            Account account = Account.builder()
                    .coin(createCoin(1L, "Bitcoin", "BTC", false))
                    .accountAddress("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh")
                    .addressRegistryStatus(AddressRegistryStatus.ACTIVE)
                    .build();
            
            when(accountRepository.findByActor_EmailAndCoin_Currency(email, currency))
                    .thenReturn(Optional.of(account));
            when(coinRepository.findByCurrency(currency))
                    .thenReturn(createCoin(1L, "Bitcoin", "BTC", false));

            // when
            GetAddressResponse response = accountService.getAddressByCurrency(currency, email);

            // then
            assertNotNull(response);
            assertEquals(currency, response.getCurrency());
            assertEquals("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", response.getAddress());
            assertEquals("ACTIVE", response.getAddressRegistryStatus());
        }
    }
    @Test
    @DisplayName("계좌 주소 재등록 테스트 - 등록 중 상태")
    void reRegisterAddress_RegisteringStatus() {
        // given
        String email = "user@example.com";
        GetAddressRequest request = new GetAddressRequest("BTC", "new_address", null);

        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(() -> SecurityUtil.getRoleByEmail(email))
                    .thenReturn("ACTOR");

            Account account = Account.builder()
                    .coin(createCoin(1L, "Bitcoin", "BTC", false))
                    .accountAddress("old_address")
                    .addressRegistryStatus(AddressRegistryStatus.ACTIVE)
                    .build();

            when(accountRepository.findByActor_EmailAndCoin_Currency(email, "BTC"))
                    .thenReturn(Optional.of(account));
            when(coinRepository.findByCurrency("BTC"))
                    .thenReturn(createCoin(1L, "Bitcoin", "BTC", false));

            // when
            accountService.reRegisterAddress(email, request);

            // then
            assertEquals("new_address", account.getAccountAddress());
            assertEquals(AddressRegistryStatus.UNREGISTERED_AND_REGISTERING, account.getAddressRegistryStatus());
        }
    }

    // 헬퍼 메서드
    private Coin createCoin(Long id, String name, String currency, boolean isTag) {
        return Coin.builder()
                .id(id)
                .name(name)
                .currency(currency)
                .isTag(isTag)
                .build();
    }

    private Account createAccount(Long id, String currency, BigDecimal balance) {
        Coin coin = createCoin(id, "TestCoin", currency, false);
        return Account.builder()
                .id(id)
                .coin(coin)
                .balance(balance)
                .build();
    }
}