package dev.crepe.domain.bank.service.impl;

import dev.crepe.domain.admin.exception.AlreadyHoldAddressException;
import dev.crepe.domain.bank.exception.BankManagerNameMismatchException;
import dev.crepe.domain.bank.model.dto.request.CreateBankAccountRequest;
import dev.crepe.domain.bank.model.dto.response.GetAccountDetailResponse;
import dev.crepe.domain.bank.model.dto.response.GetCoinAccountInfoResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.service.BankAccountManageService;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.bank.service.BankTokenManageService;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.exception.MissingAccountRequestException;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.util.coin.regulation.model.dto.response.RemainingCoinBalanceResponse;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.regulation.repository.PortfolioRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountManageServiceImpl implements BankAccountManageService {

    private final AccountService accountService;
    private final BankService bankService;
    private final BankTokenManageService bankTokenManageService;
    private final ExceptionDbService exceptionDbService;
    private final PortfolioRepository portfolioRepository;

    // 은행 출금 계좌 등록
    @Transactional
    @Override
    public void createBankAccount(CreateBankAccountRequest request, String bankEmail) {

        Bank bank = bankService.findBankInfoByEmail(bankEmail);

        // 조회한 bankName과 요청의 bankName 비교
        if (!request.getManagerName().equals(bank.getManagerName())) {
            throw exceptionDbService.getException("BANK_003");
        }

        // Address 요청 정보 확인
        GetAddressRequest getAddressRequest = request.getGetAddressRequest();
        if (getAddressRequest == null) {
            throw exceptionDbService.getException("REQUEST_001");
        }

        // 계좌 등록 요청 전송
        accountService.submitAccountRegistrationRequest(getAddressRequest, bankEmail);

    }


    // 은행 출금 계좌 재등록
    @Transactional
    @Override
    public void changeBankAccount(CreateBankAccountRequest request, String bankEmail) {

        Bank bank = bankService.findBankInfoByEmail(bankEmail);

        if (!request.getManagerName().equals(bank.getManagerName())) {
            throw exceptionDbService.getException("BANK_003");
        }

        GetAddressRequest getAddressRequest = request.getGetAddressRequest();
        if (getAddressRequest == null) {
            throw exceptionDbService.getException("REQUEST_001");
        }

        accountService.reRegisterAddress( bankEmail, request.getGetAddressRequest());

    }

    // 은행 출금 계좌 해제
    @Transactional
    @Override
    public void unRegisterBankAccount(String currency, String bankEmail) {

        accountService.unRegisterAccount(bankEmail, currency);

    }



    // 코인별 계좌 정보 조회
    @Transactional(readOnly = true)
    @Override
    public GetAccountDetailResponse getAccountByCurrency(String currency, String bankEmail) {

        GetAddressResponse response = accountService.getAddressByCurrency(currency, bankEmail);

        String bankName = accountService.getAccountOwnerName(bankEmail, currency);

        // GetAccountDetailResponse 생성 및 반환
        return GetAccountDetailResponse.builder()
                .bankName(bankName)
                .addressResponse(response)
                .build();
    }


    // 은행 계좌 정보 조회
    @Transactional(readOnly = true)
    @Override
    public List<GetCoinAccountInfoResponse> getAccountInfoList(String bankEmail) {

        Bank bank = bankService.findBankInfoByEmail(bankEmail);

        List<Account> accounts = accountService.getAccountsByBankEmail(bank.getEmail());

        if (accounts.isEmpty()) {
            throw exceptionDbService.getException("ACCOUNT_001");
        }

        // 각 Account 정보를 매핑하여 GetAllAccountInfoResponse 생성
        // ACTIVE 상태의 계좌만 필터링하고 매핑
        return accounts.stream()
                .filter(a -> a.getCoin() != null) // 코인 정보가 없는 계좌는 제외
                .filter(a -> a.getAddressRegistryStatus() == AddressRegistryStatus.ACTIVE
                        || a.getAddressRegistryStatus() == AddressRegistryStatus.REGISTERING)
                .map(a -> GetCoinAccountInfoResponse.builder()
                        .bankName(a.getBank() != null ? a.getBank().getName() : null)
                        .managerName(a.getBank() != null ? a.getBank().getManagerName() : null)
                        .coinName(a.getCoin().getName())
                        .currency(a.getCoin().getCurrency())
                        .accountAddress(a.getAccountAddress())
                        .tag(a.getTag())
                        .balance(a.getBalance().toPlainString())
                        .status(a.getAddressRegistryStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void holdBankAccount(Account account) {

        if (account.getAddressRegistryStatus() == AddressRegistryStatus.HOLD) {
            throw exceptionDbService.getException("ACCOUNT_010");
        }
        accountService.holdAccount(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RemainingCoinBalanceResponse> calculateRemainingBalances(String email) {

        BankToken bankToken = bankTokenManageService.getBankTokenByEmail(email);

        // Portfolio 조회
        List<Portfolio> portfolios = portfolioRepository.findByBankToken(bankToken);

        // Portfolio에서 published_balance 계산
        Map<String, BigDecimal> publishedBalances = portfolios.stream()
                .collect(Collectors.toMap(
                        p -> p.getCoin().getCurrency(),
                        p -> p.getAmount().multiply(p.getInitialPrice())
                ));

        // Account에서 balance 조회
        List<Account> accounts = accountService.getActiveAccountsByBankEmail(email);
        Map<String, BigDecimal> accountBalances = accounts.stream()
                .filter(a -> a.getCoin() != null)
                .collect(Collectors.toMap(
                        a -> a.getCoin().getCurrency(),
                        Account::getBalance
                ));

        // published_balance - balance 계산
        List<RemainingCoinBalanceResponse> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : publishedBalances.entrySet()) {
            String currency = entry.getKey();
            BigDecimal publishedBalance = entry.getValue();
            BigDecimal accountBalance = accountBalances.getOrDefault(currency, BigDecimal.ZERO);
            BigDecimal remainingBalance = publishedBalance.subtract(accountBalance);

            String coinName = portfolios.stream()
                    .filter(p -> p.getCoin().getCurrency().equals(currency))
                    .findFirst()
                    .map(p -> p.getCoin().getName())
                    .orElse(null);

            result.add(RemainingCoinBalanceResponse.builder()
                    .coinName(coinName)
                    .currency(currency)
                    .publishedBalance(publishedBalance)
                    .accountBalance(accountBalance)
                    .remainingBalance(remainingBalance)
                    .build());
        }

        return result;

    }

}

