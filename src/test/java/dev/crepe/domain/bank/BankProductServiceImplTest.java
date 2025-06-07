package dev.crepe.domain.bank;

import dev.crepe.domain.bank.model.dto.response.GetAllProductResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.bank.service.impl.BankProductServiceImpl;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.product.model.BankProductStatus;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.product.model.dto.request.RegisterProductRequest;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.global.error.exception.CustomException;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.infra.s3.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankProductServiceImplTest {

    @Mock
    private BankRepository bankRepository;

    @Mock
    private BankTokenRepository bankTokenRepository;


    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExceptionDbService exceptionDbService;

    @InjectMocks
    private BankProductServiceImpl bankProductService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("은행 자본금 부족으로 상품 등록 실패 테스트")
    void registerProduct_Failure_InsufficientFunds() {
        // given
        String email = "bank@example.com";
        MultipartFile productImage = mock(MultipartFile.class);
        MultipartFile guideFile = mock(MultipartFile.class);
        RegisterProductRequest request = RegisterProductRequest.builder()
                .productName("Test Product")
                .type(BankProductType.INSTALLMENT)
                .budget(new BigDecimal("3000")) // 자본금 부족 금액
                .maxMonthlyPayment(new BigDecimal("500"))
                .baseRate(new BigDecimal("5.0"))
                .startDate(LocalDate.of(2023, 1, 1))
                .endDate(LocalDate.of(2023, 12, 31))
                .build();

        Bank bank = Bank.builder().id(1L).email(email).build();
        BankToken bankToken = BankToken.builder().id(1L).bank(bank).build();
        Account account = Account.builder().balance(new BigDecimal("2000")).build(); // 자본금 부족

        when(bankRepository.findByEmail(email)).thenReturn(Optional.of(bank));
        when(bankTokenRepository.findByBank(bank)).thenReturn(Optional.of(bankToken));
        when(accountRepository.findByBankAndBankToken(bank, bankToken)).thenReturn(Optional.of(account));
        when(exceptionDbService.getException("ACCOUNT_006"))
                .thenThrow(new CustomException("ACCOUNT_006", null, null));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> bankProductService.registerProduct(email, productImage, guideFile, request));
        assertEquals("ACCOUNT_006", exception.getCode());
        verify(bankRepository).findByEmail(email);
        verify(bankTokenRepository).findByBank(bank);
        verify(accountRepository).findByBankAndBankToken(bank, bankToken);
    }



    @Test
    @DisplayName("은행 이메일로 상품 조회 실패 테스트 - 은행 없음")
    void findAllProductsByBankEmail_Failure_BankNotFound() {
        // given
        String email = "bank@example.com";

        when(bankRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(exceptionDbService.getException("BANK_001"))
                .thenThrow(new IllegalArgumentException("존재하지 않는 은행 계정입니다."));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bankProductService.findAllProductsByBankEmail(email));
        assertEquals("존재하지 않는 은행 계정입니다.", exception.getMessage());
        verify(bankRepository).findByEmail(email);
    }


}