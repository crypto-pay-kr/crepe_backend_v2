package dev.crepe.domain.bank.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankAccountRequest;
import dev.crepe.domain.bank.model.dto.response.GetAllAccountInfoResponse;

import java.util.List;

public interface BankAccountService {

    void createBankAccountAddress(CreateBankAccountRequest request, String bankEmail);

    List<GetAllAccountInfoResponse> getAccountInfoList(String email);


}
