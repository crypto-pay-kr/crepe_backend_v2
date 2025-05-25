package dev.crepe.domain.bank.service.impl;

import dev.crepe.domain.admin.exception.AlreadyHoldAddressException;
import dev.crepe.domain.bank.exception.BankManagerNameMismatchException;
import dev.crepe.domain.bank.exception.BankNameMismatchException;
import dev.crepe.domain.bank.model.dto.request.CreateBankAccountRequest;
import dev.crepe.domain.bank.model.dto.response.GetAccountDetailResponse;
import dev.crepe.domain.bank.model.dto.response.GetCoinAccountInfoResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.service.BankAccountManageService;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.exception.MissingAccountRequestException;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountManageServiceImpl implements BankAccountManageService {

    private final AccountService accountService;
    private final BankService bankService;

    // 은행 출금 계좌 등록
    @Transactional
    @Override
    public void createBankAccount(CreateBankAccountRequest request, String bankEmail) {

        Bank bank = bankService.findBankInfoByEmail(bankEmail);

        // 조회한 bankName과 요청의 bankName 비교
        if (!request.getManagerName().equals(bank.getManagerName())) {
            throw new BankManagerNameMismatchException(request.getManagerName(), bank.getManagerName());
        }

        // Address 요청 정보 확인
        GetAddressRequest getAddressRequest = request.getGetAddressRequest();
        if (getAddressRequest == null) {
            throw new MissingAccountRequestException();
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
            throw new BankManagerNameMismatchException(request.getManagerName(), bank.getManagerName());
        }

        GetAddressRequest getAddressRequest = request.getGetAddressRequest();
        if (getAddressRequest == null) {
            throw new MissingAccountRequestException();
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
            throw new AccountNotFoundException(bankEmail);
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
            throw new AlreadyHoldAddressException(account.getAccountAddress());
        }
        accountService.holdAccount(account);
    }
}
