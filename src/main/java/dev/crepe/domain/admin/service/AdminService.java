package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.response.GetPendingBankTokenResponse;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.global.model.dto.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminService {

    ApiResponse<TokenResponse> adminLogin(LoginRequest request);



}
