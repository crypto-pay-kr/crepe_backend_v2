package dev.crepe.domain.bank.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankAccountRequest;
import dev.crepe.domain.bank.model.dto.response.GetAccountDetailResponse;
import dev.crepe.domain.bank.model.dto.response.GetCoinAccountInfoResponse;
import dev.crepe.domain.core.account.model.entity.Account;

import java.util.List;

public interface BankAccountManageService {

    void createBankAccount(CreateBankAccountRequest request, String bankEmail);

    void changeBankAccount(CreateBankAccountRequest request, String bankEmail);

    GetAccountDetailResponse getAccountByCurrency(String currency, String bankEmail);

    List<GetCoinAccountInfoResponse> getAccountInfoList(String bankEmail);

    void unRegisterBankAccount( String currency,String email);

    void holdBankAccount(Account account);
}
