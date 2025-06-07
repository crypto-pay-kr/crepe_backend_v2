package dev.crepe.domain.core.transfer;

import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;
import dev.crepe.domain.core.transfer.service.Impl.WithdrawServiceImpl;
import dev.crepe.domain.core.transfer.service.WithdrawService;
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

public class WithdrawServiceImplTest {


    @InjectMocks
    private WithdrawServiceImpl withdrawService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @Mock
    private ExceptionDbService exceptionDbService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("주소가 등록되지 않은 출금 요청에 대한 예외 검증")
    void requestWithdraw_Failure_AddressNotRegistered() {
        // given
        String email = "user@example.com";
        String currency = "BTC";
        String amount = "500";

        Account account = Account.builder()
                .addressRegistryStatus(AddressRegistryStatus.NOT_REGISTERED)
                .balance(BigDecimal.valueOf(1000))
                .build();

        GetWithdrawRequest request = GetWithdrawRequest.builder()
                .currency(currency)
                .amount(amount)
                .build();

        // Mock 설정
        when(accountRepository.findByActor_EmailAndCoin_Currency(email, currency))
                .thenReturn(Optional.of(account));
        when(exceptionDbService.getException("ACCOUNT_005"))
                .thenReturn(new CustomException("ACCOUNT_005", null, null));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            withdrawService.requestWithdraw(request, email);
        });

        assertEquals("ACCOUNT_005", exception.getCode());
        verify(transactionHistoryRepository, never()).save(any(TransactionHistory.class));
    }


}
