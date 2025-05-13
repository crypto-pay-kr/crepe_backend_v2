package dev.crepe.domain.bank.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankAccountRequest;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;

import java.util.List;

public interface BankAccountService {

    void createBankAccountAddress(CreateBankAccountRequest request, String bankEmail);


    void reRegisterBankAccountAddress(GetAddressRequest request, String bankEmail);

    GetAddressResponse getAddressByCurrency(String currency, String bankEmail);

    List<GetBalanceResponse> getBalanceList(String email);

    GetBalanceResponse getBalanceByCurrency(String currency, String email);

}
