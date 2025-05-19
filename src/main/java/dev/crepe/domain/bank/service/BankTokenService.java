package dev.crepe.domain.bank.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.response.GetTokenAccountInfoResponse;
import dev.crepe.domain.bank.model.dto.response.GetTokenHistoryResponse;
import dev.crepe.global.model.dto.GetPaginationRequest;

import java.util.List;
import java.util.Optional;

public interface BankTokenService {

    void createBankToken(CreateBankTokenRequest request, String bankEmail);

    void recreateBankToken(ReCreateBankTokenRequest request, String bankEmail);

    GetTokenAccountInfoResponse getAccountByBankToken(String bankEmail);

    List<GetTokenHistoryResponse> getTokenHistory(GetPaginationRequest request);
}
