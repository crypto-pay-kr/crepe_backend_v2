package dev.crepe.domain.bank.service.impl;

import dev.crepe.domain.bank.model.dto.request.CreateBankAccountRequest;
import dev.crepe.domain.bank.model.dto.response.GetAccountDetailResponse;
import dev.crepe.domain.bank.model.dto.response.GetCoinAccountInfoResponse;
import dev.crepe.domain.bank.service.BankAccountService;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    // 은행 출금 계좌 등록
    @Transactional
    @Override
    public void createBankAccount(CreateBankAccountRequest request, String bankEmail) {

        String bankName = request.getBankName();
        if(bankName == null || bankName.isEmpty()) {
            throw new IllegalArgumentException("은행 이름은 필수입니다.");
        }

        // Address 요청 정보 확인
        GetAddressRequest getAddressRequest = request.getGetAddressRequest();
        if (getAddressRequest == null) {
            throw new IllegalArgumentException("Address 요청 정보가 필요합니다.");
        }

        // 계좌 등록 요청 전송
        accountService.submitAccountRegistrationRequest(getAddressRequest, bankEmail);

    }


    // 은행 출금 계좌 재등록
    @Transactional
    @Override
    public void changeBankAccount(CreateBankAccountRequest request, String bankEmail) {

        String bankName = request.getBankName();
        if(bankName == null || bankName.isEmpty()) {
            throw new IllegalArgumentException("은행 이름은 필수입니다.");
        }

        GetAddressRequest getAddressRequest = request.getGetAddressRequest();
        if (getAddressRequest == null) {
            throw new IllegalArgumentException("Address 요청 정보가 필요합니다.");
        }

        accountService.reRegisterAddress( bankEmail, request.getGetAddressRequest());

    }


    // 코인별 계좌 정보 조회

    @Transactional(readOnly = true)
    @Override
    public GetAccountDetailResponse getAccountByCurrency(String currency, String email) {

        GetAddressResponse response = accountService.getAddressByCurrency(currency, email);

        // email을 통해 Bank 정보 조회
        Account account = accountRepository.findByBank_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException("해당 이메일과 통화로 등록된 계좌가 없습니다."));

        String bankName = account.getBank() != null ? account.getBank().getName() : null;

        // GetAccountDetailResponse 생성 및 반환
        return GetAccountDetailResponse.builder()
                .bankName(bankName)
                .addressResponse(response)
                .build();
    }


    // 은행 계좌 정보 조회
    @Transactional(readOnly = true)
    @Override
    public List<GetCoinAccountInfoResponse> getAccountInfoList(String email) {

        // email 유효성 검사
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 null이거나 빈 값일 수 없습니다.");
        }

        List<Account> accounts = accountRepository.findByBank_Email(email);
        if (accounts.isEmpty()) {
            throw new AccountNotFoundException("해당 이메일로 등록된 계좌가 없습니다: " + email);
        }

        // 각 Account 정보를 매핑하여 GetAllAccountInfoResponse 생성
        // ACTIVE 상태의 계좌만 필터링하고 매핑
        return accounts.stream()
                .filter(a -> a.getCoin() != null) // 코인 정보가 없는 계좌는 제외
                .filter(a -> a.getAddressRegistryStatus() == AddressRegistryStatus.ACTIVE)
                .map(a -> GetCoinAccountInfoResponse.builder()
                        .bankname(a.getBank() != null ? a.getBank().getName() : null)
                        .coinName(a.getCoin().getName())
                        .currency(a.getCoin().getCurrency())
                        .accountAddress(a.getAccountAddress())
                        .tag(a.getTag())
                        .balance(a.getBalance().toPlainString())
                        .build())
                .collect(Collectors.toList());
    }

}
