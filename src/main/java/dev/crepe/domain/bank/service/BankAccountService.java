package dev.crepe.domain.bank.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankAccountRequest;
import dev.crepe.domain.bank.model.dto.response.GetAccountDetailResponse;
import dev.crepe.domain.bank.model.dto.response.GetAllAccountInfoResponse;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;

import java.util.List;

public interface BankAccountService {

    void createBankAccountAddress(CreateBankAccountRequest request, String bankEmail);

    void changeBankAccount(CreateBankAccountRequest request, String bankEmail);

    GetAccountDetailResponse getAccountByCurrency(String currency, String bankEmail);

    List<GetAllAccountInfoResponse> getAccountInfoList(String bankEmail);


}
