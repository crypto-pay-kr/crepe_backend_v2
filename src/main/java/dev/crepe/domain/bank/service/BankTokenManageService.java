package dev.crepe.domain.bank.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.response.GetTokenAccountInfoResponse;
import dev.crepe.domain.bank.model.dto.response.GetTokenHistoryResponse;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.global.model.dto.GetPaginationRequest;

import java.math.BigDecimal;
import java.util.List;

public interface BankTokenManageService {

    void createBankToken(CreateBankTokenRequest request, String bankEmail);

    void recreateBankToken(ReCreateBankTokenRequest request, String bankEmail);

    GetTokenAccountInfoResponse getAccountByBankToken(String bankEmail);

    List<GetTokenHistoryResponse> getTokenHistory(GetPaginationRequest request);

    BigDecimal getLatestTokenPrice(String bankEmail);

    BigDecimal getTotalTokenVolume(String bankEmail);

    BankToken getBankTokenByEmail(String bankEmail);
}
