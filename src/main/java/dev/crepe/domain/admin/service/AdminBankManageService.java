package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.request.ChangeBankStatusRequest;
import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.admin.dto.response.*;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import dev.crepe.domain.bank.model.dto.response.GetBankInfoDetailResponse;
import dev.crepe.domain.bank.model.dto.response.GetCoinAccountInfoResponse;
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

    List<GetCoinAccountInfoResponse> getBankAccountByAdmin(Long bankId);

    void holdBankAddress(Long accountId);

    GetProductDetailResponse getBankProductDetail(Long bankId, Long productId);

}
