package dev.crepe.domain.bank.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.response.GetTokenAccountInfoResponse;
import dev.crepe.domain.core.account.model.entity.Account;

import java.util.Optional;

public interface BankTokenService {

    void createBankToken(CreateBankTokenRequest request, String bankEmail);

    GetTokenAccountInfoResponse getAccountByBankToken(String bankEmail);
}
