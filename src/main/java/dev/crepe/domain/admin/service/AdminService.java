package dev.crepe.domain.admin.service;

import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.global.model.dto.ApiResponse;


import java.util.List;

public interface AdminService {

    ApiResponse<TokenResponse> adminLogin(LoginRequest request);



}
