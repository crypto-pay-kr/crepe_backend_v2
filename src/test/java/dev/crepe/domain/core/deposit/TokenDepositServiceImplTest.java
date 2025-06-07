package dev.crepe.domain.core.deposit;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.history.subscribe.model.SubscribeHistoryType;
import dev.crepe.domain.core.util.history.subscribe.repository.SubscribeHistoryRepository;
import dev.crepe.global.error.exception.CustomException;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.domain.core.deposit.service.impl.TokenDepositServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenDepositServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SubscribeRepository subscribeRepository;

    @Mock
    private SubscribeHistoryRepository subscribeHistoryRepository;

    @Mock
    private ExceptionDbService exceptionDbService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TokenDepositServiceImpl tokenDepositService;

    @Test
    @DisplayName("상품 예치 성공 테스트")
    void depositToProduct_Success() {
        // given
        String userEmail = "user@example.com";
        Long subscribeId = 1L;
        BigDecimal amount = new BigDecimal("1000");

        Product product = Product.builder()
                .type(BankProductType.VOUCHER)
                .bankToken(BankToken.builder().id(1L).build())
                .baseInterestRate(5.0f)
                .build();

        Subscribe subscribe = Subscribe.builder()
                .id(subscribeId)
                .status(SubscribeStatus.ACTIVE)
                .product(product)
                .balance(BigDecimal.ZERO)
                .build();

        Account account = Account.builder()
                .balance(new BigDecimal("2000"))
                .build();

        BigDecimal discount = BigDecimal.valueOf(product.getBaseInterestRate());
        BigDecimal discountRatio = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100)));
        BigDecimal discountedAmount = amount.multiply(discountRatio);

        when(subscribeRepository.findById(subscribeId)).thenReturn(Optional.of(subscribe));
        when(accountRepository.findByActor_EmailAndBankTokenId(userEmail, product.getBankToken().getId()))
                .thenReturn(Optional.of(account));
        doNothing().when(accountService).validateAndReduceAmount(account, discountedAmount);
        when(subscribeHistoryRepository.save(any())).thenReturn(null);

        // when
        String result = tokenDepositService.depositToProduct(userEmail, subscribeId, amount);

        // then
        assertEquals("예치 완료", result);
        assertEquals(amount, subscribe.getBalance());
        verify(subscribeRepository).save(subscribe);
        verify(subscribeHistoryRepository).save(any());
    }

    @Test
    @DisplayName("한 달 최대 예치 금액 초과 예외 테스트")
    void depositToProduct_ExceedMonthlyLimit_Exception() {
        // given
        String userEmail = "user@example.com";
        Long subscribeId = 1L;
        BigDecimal amount = new BigDecimal("2000"); // 초과 금액

        Product product = Product.builder()
                .type(BankProductType.INSTALLMENT)
                .bankToken(BankToken.builder().id(1L).build())
                .maxMonthlyPayment(new BigDecimal("1500")) // 한 달 최대 예치 금액 설정
                .build();

        Subscribe subscribe = Subscribe.builder()
                .id(subscribeId)
                .status(SubscribeStatus.ACTIVE)
                .product(product)
                .balance(new BigDecimal("1000")) // 이미 예치된 금액
                .build();

        Account account = Account.builder()
                .balance(new BigDecimal("5000"))
                .build();

        when(subscribeRepository.findById(subscribeId)).thenReturn(Optional.of(subscribe));
        when(accountRepository.findByActor_EmailAndBankTokenId(userEmail, product.getBankToken().getId()))
                .thenReturn(Optional.of(account));
        when(subscribeHistoryRepository.sumMonthlyDeposit(
                eq(subscribe),
                eq(SubscribeHistoryType.DEPOSIT),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("1000")); // 이 달의 누적 예치 금액
        when(exceptionDbService.getException("PRODUCT_DEPOSIT_002"))
                .thenReturn(new CustomException("PRODUCT_DEPOSIT_002", null, null));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> tokenDepositService.depositToProduct(userEmail, subscribeId, amount));
        assertEquals("PRODUCT_DEPOSIT_002", exception.getCode());
    }


    @Test
    @DisplayName("상품 미존재 예외 테스트")
    void depositToProduct_SubscribeNotFound_Exception() {
        // given
        String userEmail = "user@example.com";
        Long subscribeId = 999L;
        BigDecimal amount = new BigDecimal("1000");

        CustomException customException = new CustomException("SUBSCRIBE_004", null, null);
        when(subscribeRepository.findById(subscribeId)).thenThrow(customException);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> tokenDepositService.depositToProduct(userEmail, subscribeId, amount));
        assertEquals("SUBSCRIBE_004", exception.getCode());
    }

    @Test
    @DisplayName("계좌 잔액 부족 예외 테스트")
    void depositToProduct_InsufficientBalance_Exception() {
        // given
        String userEmail = "user@example.com";
        Long subscribeId = 1L;
        BigDecimal amount = new BigDecimal("3000");

        Product product = Product.builder()
                .type(BankProductType.VOUCHER)
                .bankToken(BankToken.builder().id(1L).build())
                .build();

        Subscribe subscribe = Subscribe.builder()
                .id(subscribeId)
                .status(SubscribeStatus.ACTIVE)
                .product(product)
                .balance(BigDecimal.ZERO)
                .build();

        Account account = Account.builder()
                .balance(new BigDecimal("2000"))
                .build();

        when(subscribeRepository.findById(subscribeId)).thenReturn(Optional.of(subscribe));
        when(accountRepository.findByActor_EmailAndBankTokenId(userEmail, product.getBankToken().getId()))
                .thenReturn(Optional.of(account));
        when(exceptionDbService.getException("ACCOUNT_001"))
                .thenReturn(new CustomException("ACCOUNT_001", null, null));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> tokenDepositService.depositToProduct(userEmail, subscribeId, amount));
        assertEquals("ACCOUNT_001", exception.getCode());
    }
}