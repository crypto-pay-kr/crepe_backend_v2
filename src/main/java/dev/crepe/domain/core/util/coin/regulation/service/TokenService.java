package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;

public interface TokenService {

    void approveBankTokenRequest(Long tokenHistoryId);

    void rejectBankTokenRequest(RejectBankTokenRequest request, Long tokenHistoryId);

}
