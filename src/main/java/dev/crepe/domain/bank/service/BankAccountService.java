package dev.crepe.domain.bank.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankAccountRequest;
import dev.crepe.domain.bank.model.dto.response.GetAccountDetailResponse;
import dev.crepe.domain.bank.model.dto.response.GetCoinAccountInfoResponse;

import java.util.List;

public interface BankAccountService {

    void createBankAccount(CreateBankAccountRequest request, String bankEmail);

    void changeBankAccount(CreateBankAccountRequest request, String bankEmail);

    GetAccountDetailResponse getAccountByCurrency(String currency, String bankEmail);

    List<GetCoinAccountInfoResponse> getAccountInfoList(String bankEmail);


}
