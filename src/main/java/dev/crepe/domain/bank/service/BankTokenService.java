package dev.crepe.domain.bank.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;

public interface BankTokenService {

    void createBankToken(CreateBankTokenRequest request, String bankEmail);
}
