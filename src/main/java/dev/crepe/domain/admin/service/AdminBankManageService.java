package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.response.GetAllBankTokenResponse;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminBankManageService {

    void bankSignup(BankSignupDataRequest request, MultipartFile bankCiImage);

    List<GetAllBankTokenResponse> getAllBankTokenResponseList(int page, int size);

    void approveBankTokenRequest(Long tokenId);

    void rejectBankTokenRequest(Long tokenId);
}
