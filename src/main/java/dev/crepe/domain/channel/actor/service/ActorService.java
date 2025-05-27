package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.admin.dto.request.ChangeActorStatusRequest;
import dev.crepe.domain.admin.dto.response.ChangeActorStatusResponse;
import dev.crepe.domain.channel.actor.model.dto.request.*;
import dev.crepe.domain.channel.actor.model.dto.response.GetFinancialSummaryResponse;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.core.subscribe.model.dto.request.SubscribeProductRequest;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeProductResponse;
import dev.crepe.global.model.dto.ApiResponse;
import dev.crepe.infra.naver.ocr.id.entity.dto.IdCardOcrResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;


public interface ActorService {

    //********** 회원정보 중복 체크 **********/
    boolean isEmailExists(String email);

    boolean isNicknameExists(String nickname);

    //********** 회원 정보 수정 **********/
    ResponseEntity<Void> changePassword(ChangePasswordRequest request, String userEmail);

    ResponseEntity<Void> changePhone(ChangePhoneRequest request, String email);

    ResponseEntity<Void> changeName(ChangeNameRequest request, String email);


    //********** 로그인 **********/

    ApiResponse<TokenResponse> login(LoginRequest request);
    ResponseEntity<Void> addOccupationName(AddOccupationRequest request, String userEmail);
    GetFinancialSummaryResponse checkIncome(String userEmail);
    ResponseEntity<String> updateFromIdCard(String userEmail,IdCardOcrResponse idCardResponse);

    // role 역할별 수 세기
    Map<String, Long> getRoleCounts();

    // actor 정지, 해제
    ChangeActorStatusResponse changeActorStatus(ChangeActorStatusRequest request);

}
