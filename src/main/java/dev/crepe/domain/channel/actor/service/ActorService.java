package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.channel.actor.model.dto.request.*;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.core.subscribe.model.dto.request.SubscribeProductRequest;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeProductResponse;
import dev.crepe.global.model.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface ActorService {

    //********** 회원 정보 수정 **********/
    ResponseEntity<Void> changePassword(ChangePasswordRequest request, String userEmail);

    ResponseEntity<Void> changePhone(ChangePhoneRequest request, String email);

    ResponseEntity<Void> changeName(ChangeNameRequest request, String email);


    //********** 로그인 **********/

    ApiResponse<TokenResponse> login(LoginRequest request);


    SubscribeProductResponse subscribeProduct(String userEmail, SubscribeProductRequest request);
    ResponseEntity<Void> addOccupationName(AddOccupationRequest request, String userEmail);
    ResponseEntity<String> checkIncome(String useEmail);
}
