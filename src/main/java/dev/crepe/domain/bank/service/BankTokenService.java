package dev.crepe.domain.bank.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.response.GetTokenAccountInfoResponse;

import java.util.Optional;

public interface BankTokenService {

    void createBankToken(CreateBankTokenRequest request, String bankEmail);

    void reCreateBankToken(ReCreateBankTokenRequest request, String bankEmail);

    GetTokenAccountInfoResponse getAccountByBankToken(String bankEmail);
}
