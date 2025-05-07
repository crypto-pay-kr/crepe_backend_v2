package dev.crepe.domain.channel.actor.user.service;

import dev.crepe.domain.channel.actor.user.model.dto.ChangeNicknameRequest;
import dev.crepe.domain.channel.actor.user.model.dto.UserInfoResponse;
import dev.crepe.domain.channel.actor.user.model.dto.UserSignupRequest;
import dev.crepe.global.model.dto.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {

//    ApiResponse<ResponseEntity<Void>> signup(UserSignupRequest request);

    void changeNickname(ChangeNicknameRequest request, String userEmail);

    UserInfoResponse getUserInfo(String email);
}
