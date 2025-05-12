package dev.crepe.domain.admin.service;

import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.global.model.dto.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AdminService {

    ApiResponse<TokenResponse> login(LoginRequest request);
    void bankIdActivate(BankSignupDataRequest request, MultipartFile bankCiImage);

}
