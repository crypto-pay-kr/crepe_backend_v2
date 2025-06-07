package dev.crepe.domain.core.transfer;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.transfer.model.dto.requset.GetTransferRequest;
import dev.crepe.domain.core.transfer.service.Impl.TransferServiceImpl;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.global.error.exception.CustomException;
import dev.crepe.global.error.exception.ExceptionDbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TransferServiceImplTest {

    @InjectMocks
    private TransferServiceImpl transferService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @Mock
    private ExceptionDbService exceptionDbService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("정상 이체 시 송금/수취 계좌 잔액 변화 검증")
    void requestTransfer_Success_BalanceChange() {
        // given
        String senderEmail = "sender@example.com";
        String receiverEmail = "receiver@example.com";
        String currency = "BTC";
        BigDecimal transferAmount = BigDecimal.valueOf(500);
        BigDecimal senderInitialBalance = BigDecimal.valueOf(1000);
        BigDecimal receiverInitialBalance = BigDecimal.valueOf(200);

        Account senderAccount = Account.builder()
                .balance(senderInitialBalance)
                .build();

        Account receiverAccount = Account.builder()
                .balance(receiverInitialBalance)
                .build();

        Actor senderActor = Actor.builder().email(senderEmail).build();
        Actor receiverActor = Actor.builder().email(receiverEmail).build();

        GetTransferRequest request = GetTransferRequest.builder()
                .currency(currency)
                .amount(transferAmount)
                .receiverEmail(receiverEmail)
                .build();

        // Mock 설정
        when(accountRepository.findByActor_EmailAndCoin_Currency(senderEmail, currency))
                .thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByActor_EmailAndCoin_Currency(receiverEmail, currency))
                .thenReturn(Optional.of(receiverAccount));
        when(actorRepository.findByEmail(senderEmail)).thenReturn(Optional.of(senderActor));
        when(actorRepository.findByEmail(receiverEmail)).thenReturn(Optional.of(receiverActor));
        // 판매자 계좌 잔액 감소
        doAnswer(invocation -> {
            senderAccount.reduceAmount(transferAmount);
            return null;
        }).when(accountService).validateAndReduceAmount(senderAccount, transferAmount);

        // when
        transferService.requestTransfer(request, senderEmail);

        // then
        assertEquals(senderInitialBalance.subtract(transferAmount), senderAccount.getBalance());
        assertEquals(receiverInitialBalance.add(transferAmount), receiverAccount.getBalance());
        verify(accountService, times(1)).validateAndReduceAmount(senderAccount, transferAmount);
        verify(transactionHistoryRepository, times(2)).save(any(TransactionHistory.class));
    }

    @Test
    @DisplayName("잔액 부족 시 예외 검증")
    void requestTransfer_Failure_InsufficientBalance() {
        // given
        String senderEmail = "sender@example.com";
        String receiverEmail = "receiver@example.com";
        String currency = "BTC";
        BigDecimal transferAmount = BigDecimal.valueOf(1500); // 송금 금액이 잔액보다 큼
        BigDecimal senderInitialBalance = BigDecimal.valueOf(1000);
        BigDecimal receiverInitialBalance = BigDecimal.valueOf(200);

        Account senderAccount = Account.builder()
                .balance(senderInitialBalance)
                .build();

        Account receiverAccount = Account.builder()
                .balance(receiverInitialBalance)
                .build();

        Actor senderActor = Actor.builder().email(senderEmail).build();
        Actor receiverActor = Actor.builder().email(receiverEmail).build();

        GetTransferRequest request = GetTransferRequest.builder()
                .currency(currency)
                .amount(transferAmount)
                .receiverEmail(receiverEmail)
                .build();

        // Mock 설정
        when(accountRepository.findByActor_EmailAndCoin_Currency(senderEmail, currency))
                .thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByActor_EmailAndCoin_Currency(receiverEmail, currency))
                .thenReturn(Optional.of(receiverAccount));
        when(actorRepository.findByEmail(senderEmail)).thenReturn(Optional.of(senderActor));
        when(actorRepository.findByEmail(receiverEmail)).thenReturn(Optional.of(receiverActor));
        when(exceptionDbService.getException("ACCOUNT_006"))
                .thenReturn(new CustomException("ACCOUNT_006", null, null));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            transferService.requestTransfer(request, senderEmail);
        });

        assertEquals("ACCOUNT_006", exception.getCode());
        verify(transactionHistoryRepository, never()).save(any(TransactionHistory.class));
    }
}