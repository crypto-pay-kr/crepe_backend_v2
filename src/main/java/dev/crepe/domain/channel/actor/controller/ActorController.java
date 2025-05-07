package dev.crepe.domain.channel.actor.controller;


import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.channel.actor.model.dto.request.ChangeNameRequest;
import dev.crepe.domain.channel.actor.model.dto.request.ChangePasswordRequest;
import dev.crepe.domain.channel.actor.model.dto.request.ChangePhoneRequest;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.service.ActorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class ActorController {

    private final ActorService actorService;


    @PostMapping("/login")
    @Operation(summary = "유저, 가맹점 로그인", description = "회원 로그인")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = actorService.login(request).getData(); // ApiResponse에서 데이터 추출
        return ResponseEntity.ok(tokenResponse);
    }


    //******************************************** 회원 정보 수정 start ********************************************/

    @Operation(summary = "비밀번호 변경", description = "가맹점 회원의 비밀번호를 변경합니다.")
    @PatchMapping("/change/password")
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, AppAuthentication auth) {
        actorService.changePassword(request, auth.getUserEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "휴대폰번호 변경", description = "가맹점 회원의 휴대폰 번호를 변경합니다.")
    @PatchMapping("/change/phone")
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<Void> changePhone(@Valid @RequestBody ChangePhoneRequest request, AppAuthentication auth) {
        actorService.changePhone(request, auth.getUserEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "이름 변경", description = "가맹점, 또는 회원의 이름을 변경합니다.")
    @PatchMapping("/change/name")
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<Void> changeStoreName(@Valid @RequestBody ChangeNameRequest request, AppAuthentication auth) {
        actorService.changeName(request, auth.getUserEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    //******************************************** 회원 정보 수정 end ********************************************/


}
