package dev.crepe.domain.bank.service.impl;

import dev.crepe.domain.bank.model.dto.request.CreateBankAccountRequest;
import dev.crepe.domain.bank.service.BankAccountService;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import dev.crepe.domain.core.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final AccountService accountService;

    @Override
    public void createBankAccountAddress(CreateBankAccountRequest request, String bankEmail) {

        String bankName = request.getBankName();
        if(bankName == null || bankName.isEmpty()) {
            throw new IllegalArgumentException("Bank name is required");
        }


        // Address 요청 정보 확인
        GetAddressRequest getAddressRequest = request.getGetAddressRequest();
        if (getAddressRequest == null) {
            throw new IllegalArgumentException("Address 요청 정보가 필요합니다.");
        }

        // 계좌 등록 요청 전송
        accountService.submitAccountRegistrationRequest(getAddressRequest, bankEmail);

    }

    @Override
    public void reRegisterBankAccountAddress(GetAddressRequest request, String bankEmail) {
        accountService.reRegisterAddress(bankEmail, request);

    }

    @Override
    public GetAddressResponse getAddressByCurrency(String currency, String bankEmail) {
        return accountService.getAddressByCurrency(currency, bankEmail);
    }

    @Override
    public List<GetBalanceResponse> getBalanceList(String email) {
        return accountService.getBalanceList(email);
    }

    @Override
    public GetBalanceResponse getBalanceByCurrency(String currency, String email) {
        return accountService.getBalanceByCurrency(email, currency);
    }
}
