package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.admin.dto.response.GetAllBankTokenResponse;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface TokenService {

    List<GetAllBankTokenResponse> getAllBankTokenResponseList(PageRequest pageRequest);

    void approveBankTokenRequest(Long tokenHistoryId);

    void rejectBankTokenRequest(RejectBankTokenRequest request, Long tokenHistoryId);

}
