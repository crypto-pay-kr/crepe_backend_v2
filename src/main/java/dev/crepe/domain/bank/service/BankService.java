package dev.crepe.domain.bank.service;

import dev.crepe.domain.admin.dto.request.ChangeBankStatusRequest;
import dev.crepe.domain.admin.dto.response.GetAllBankResponse;
import dev.crepe.domain.admin.dto.response.GetAllSuspendedBankResponse;
import dev.crepe.domain.bank.model.dto.response.GetCoinAccountInfoResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.product.model.dto.request.RegisterProductRequest;
import dev.crepe.domain.core.product.model.dto.response.RegisterProductResponse;
import org.springframework.web.multipart.MultipartFile;
import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.model.dto.request.ChangeBankPhoneRequest;
import dev.crepe.domain.bank.model.dto.response.GetBankInfoDetailResponse;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.global.model.dto.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;


public interface BankService {

    ApiResponse<ResponseEntity<Void>> signup(BankDataRequest request);

   ApiResponse<TokenResponse> login(LoginRequest request);

    GetBankInfoDetailResponse getBankAllDetails(String bankEmail);

    Bank findBankInfoByEmail(String email);

    ResponseEntity<Void> changePhone(ChangeBankPhoneRequest request, String bankEmail);

    void changeBankCI(MultipartFile ciImage, String email);

    List<GetAllBankResponse> getAllActiveBankList();

    void changeBankStatus(ChangeBankStatusRequest request);

    List<GetAllSuspendedBankResponse> getAllSuspendedBankList();

    List<GetCoinAccountInfoResponse> getBankAccountByAdmin(Long bankId);
}
