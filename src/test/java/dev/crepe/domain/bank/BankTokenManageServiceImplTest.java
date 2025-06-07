package dev.crepe.domain.bank;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.coin.regulation.service.BankTokenInfoService;
import dev.crepe.domain.core.util.coin.regulation.service.PortfolioService;
import dev.crepe.domain.core.util.coin.regulation.service.TokenSetupService;
import dev.crepe.domain.core.util.history.token.service.TokenHistoryService;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import dev.crepe.global.error.exception.CustomException;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.domain.bank.service.impl.BankTokenManageServiceImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankTokenManageServiceImplTest {

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private BankTokenInfoService bankTokenInfoService;

    @Mock
    private BankService bankService;

    @Mock
    private UpbitExchangeService upbitExchangeService;

    @Mock
    private TokenSetupService tokenSetupService;

    @Mock
    private AccountService accountService;

    @Mock
    private TokenHistoryService tokenHistoryService;

    @Mock
    private BankTokenRepository bankTokenRepository;

    @InjectMocks
    private BankTokenManageServiceImpl bankTokenManageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("BankToken 발행 성공 테스트")
    void createBankToken_Success() {
        // given
        String bankEmail = "bank@example.com";
        CreateBankTokenRequest request = CreateBankTokenRequest.builder()
                .portfolioCoins(List.of(
                        CreateBankTokenRequest.CoinInfo.builder()
                                .currency("BTC")
                                .currentPrice(BigDecimal.valueOf(30000))
                                .build()
                ))
                .build();
        Bank bank = Bank.builder().email(bankEmail).build();
        BankToken bankToken = BankToken.builder()
                .name("TestToken")
                .status(BankTokenStatus.PENDING)
                .build();

        doReturn(bank).when(bankService).findBankInfoByEmail(bankEmail);
        doNothing().when(portfolioService).validatePortfolioConstitute(any(), eq(bankEmail));
        doNothing().when(upbitExchangeService).validateRateWithinThreshold(any(), any(), any());
        doReturn(bankToken).when(tokenSetupService).requestTokenGenerate(request, bank);
        doNothing().when(accountService).createBankTokenAccount(bankToken);

        // when
        bankTokenManageService.createBankToken(request, bankEmail);

        // then
        verify(bankService).findBankInfoByEmail(bankEmail);
        verify(portfolioService).validatePortfolioConstitute(any(), eq(bankEmail));
        verify(upbitExchangeService).validateRateWithinThreshold(any(), any(), any());
        verify(tokenSetupService).requestTokenGenerate(request, bank);
        verify(accountService).createBankTokenAccount(bankToken);
    }


    @Test
    @DisplayName("기존 포트폴리오 조건 미충족으로 재발행 실패 테스트")
    void recreateBankToken_Failure_InvalidPortfolio() {
        // given
        String bankEmail = "bank@example.com";
        ReCreateBankTokenRequest request = ReCreateBankTokenRequest.builder()
                .portfolioCoins(List.of(
                        ReCreateBankTokenRequest.CoinInfo.builder()
                                .currency("ETH")
                                .currentPrice(BigDecimal.valueOf(2000))
                                .build()
                ))
                .build();

        Bank bank = Bank.builder().email(bankEmail).build();
        BankToken bankToken = BankToken.builder()
                .name("TestToken")
                .status(BankTokenStatus.PENDING)
                .bank(bank)
                .build();

        // Mock 설정
        when(bankService.findBankInfoByEmail(bankEmail)).thenReturn(bank);
        when(bankTokenInfoService.findByBank(bank)).thenReturn(bankToken);
        when(tokenHistoryService.findByBankTokenAndStatus(bankToken, BankTokenStatus.PENDING)).thenReturn(Optional.empty());
        doThrow(new CustomException("PORTFOLIO_005", null, null))
                .when(portfolioService).revalidatePortfolioConstitute(request.getPortfolioCoins(), bankEmail);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> bankTokenManageService.recreateBankToken(request, bankEmail));

        assertEquals("PORTFOLIO_005", exception.getCode());
        verify(bankService).findBankInfoByEmail(bankEmail);
        verify(bankTokenInfoService).findByBank(bank);
        verify(tokenHistoryService).findByBankTokenAndStatus(bankToken, BankTokenStatus.PENDING);
        verify(portfolioService).revalidatePortfolioConstitute(request.getPortfolioCoins(), bankEmail);
    }

    @Test
    @DisplayName("createBankToken 트랜잭션 롤백 테스트")
    @Transactional
    void createBankToken_RollbackOnException() {
        // given
        String bankEmail = "bank@example.com";
        CreateBankTokenRequest request = CreateBankTokenRequest.builder()
                .portfolioCoins(List.of(
                        CreateBankTokenRequest.CoinInfo.builder()
                                .currency("BTC")
                                .currentPrice(BigDecimal.valueOf(30000))
                                .build()
                ))
                .build();
        Bank bank = Bank.builder().email(bankEmail).build();

        // Mock 설정
        when(bankService.findBankInfoByEmail(bankEmail)).thenReturn(bank);
        doThrow(new CustomException("PORTFOLIO_005", null, null))
                .when(portfolioService).validatePortfolioConstitute(any(), eq(bankEmail));

        // when & then
        assertThrows(CustomException.class, () -> {
            bankTokenManageService.createBankToken(request, bankEmail);
        });

        // verify
        verify(bankTokenRepository, never()).save(any(BankToken.class));
    }





}