package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.request.ChangeBankStatusRequest;
import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.admin.dto.response.GetAllBankResponse;
import dev.crepe.domain.admin.dto.response.GetAllBankTokenResponse;
import dev.crepe.domain.admin.dto.response.GetAllProductResponse;
import dev.crepe.domain.admin.dto.response.GetAllSuspendedBankResponse;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminBankManageService {

    void bankSignup(BankSignupDataRequest request, MultipartFile bankCiImage);

    List<GetAllBankTokenResponse> getAllBankTokenResponseList(int page, int size);

    void approveBankTokenRequest(Long tokenHistoryId);

    void rejectBankTokenRequest(RejectBankTokenRequest request, Long tokenHistoryId);

    List<GetAllProductResponse> getAllBankProducts(Long bankId);

    List<GetAllProductResponse> getSuspendedBankProducts(Long bankId);

    List<GetAllBankResponse> getAllActiveBankInfoList();

    void changeBankStatus(ChangeBankStatusRequest request);

    List<GetAllSuspendedBankResponse> getAllSuspendedBankInfoList();
}
