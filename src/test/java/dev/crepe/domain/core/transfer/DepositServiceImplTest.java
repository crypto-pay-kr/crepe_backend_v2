package dev.crepe.domain.core.transfer;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.transfer.model.dto.requset.GetDepositRequest;
import dev.crepe.domain.core.transfer.model.dto.response.GetDepositResponse;
import dev.crepe.domain.core.transfer.service.Impl.DepositServiceImpl;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.upbit.Service.UpbitDepositService;
import dev.crepe.global.error.exception.CustomException;
import dev.crepe.global.error.exception.ExceptionDbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DepositServiceImplTest {


    @InjectMocks
    private DepositServiceImpl depositService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private CoinRepository coinRepository;

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @Mock
    private UpbitDepositService upbitDepositService;

    @Mock
    private ExceptionDbService exceptionDbService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    // 정상 입금 시 잔액 증가 검증
    @Test
    @DisplayName("정상 입금 시 잔액 증가 검증")
    void requestDeposit_Success_BalanceIncrease() {
        // given
        String email = "test@example.com";
        String txid = "validTxid";
        String currency = "BTC";
        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        BigDecimal depositAmount = BigDecimal.valueOf(500);

        Account account = Account.builder()
                .balance(initialBalance)
                .build();

        GetDepositResponse depositResponse = GetDepositResponse.builder()
                .amount(depositAmount.toString())
                .state(TransactionStatus.ACCEPTED.name())
                .build();


        Coin coin = Coin.builder()
                .currency(currency)
                .build();

        // Mock 설정
        when(actorRepository.findByEmail(email))
                .thenReturn(Optional.of(Actor.builder().email(email).build()));
        when(coinRepository.findByCurrency(currency))
                .thenReturn(coin);
        when(accountRepository.findByActor_EmailAndCoin_Currency(email, currency))
                .thenReturn(Optional.of(account));
        when(transactionHistoryRepository.existsByTransactionId(txid))
                .thenReturn(false);
        when(upbitDepositService.getDepositListById(currency, txid))
                .thenReturn(List.of(depositResponse));

        // when
        depositService.requestDeposit(
                GetDepositRequest.builder().txid(txid).currency(currency).build(),
                email
        );

        // then
        assertEquals(initialBalance.add(depositAmount), account.getBalance());
        verify(transactionHistoryRepository).save(org.mockito.ArgumentMatchers.any(TransactionHistory.class));
    }

    // 거래 ID에 정확히 하나의 입금 내역만 유효하도록 검증
    @Test
    @DisplayName("거래 ID에 정확히 하나의 입금 내역만 유효하도록 검증")
    void requestDeposit_Failure_InvalidDepositListSize() {
        // given
        String email = "test@example.com";
        String txid = "invalidTxid";
        String currency = "BTC";


        GetDepositResponse depositResponse1 = GetDepositResponse.builder()
                .amount("500")
                .state(TransactionStatus.ACCEPTED.name())
                .build();

        GetDepositResponse depositResponse2 = GetDepositResponse.builder()
                .amount("300")
                .state(TransactionStatus.ACCEPTED.name())
                .build();

        Coin coin = Coin.builder()
                .currency(currency)
                .build();

        Account account = Account.builder()
                .balance(BigDecimal.valueOf(1000))
                .build();

        // Mock 설정
        when(actorRepository.findByEmail(email))
                .thenReturn(Optional.of(Actor.builder().email(email).build()));
        when(coinRepository.findByCurrency(currency))
                .thenReturn(coin);
        when(accountRepository.findByActor_EmailAndCoin_Currency(email, currency))
                .thenReturn(Optional.of(account));
        when(transactionHistoryRepository.existsByTransactionId(txid))
                .thenReturn(false);
        when(upbitDepositService.getDepositListById(currency, txid))
                .thenReturn(List.of(depositResponse1, depositResponse2));
        when(exceptionDbService.getException("DEPOSIT_003"))
                .thenThrow(new CustomException("DEPOSIT_003", null, null));


        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            depositService.requestDeposit(
                    GetDepositRequest.builder().txid(txid).currency(currency).build(),
                    email
            );
        });
        assertEquals("DEPOSIT_003", exception.getCode());
        verify(exceptionDbService).getException("DEPOSIT_003");
    }
}
