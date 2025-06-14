package dev.crepe.domain.channel.actor.user.controller;


import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.channel.actor.user.model.dto.ChangeNicknameRequest;
import dev.crepe.domain.channel.actor.user.model.dto.UserInfoResponse;
import dev.crepe.domain.channel.actor.user.model.dto.UserSignupRequest;
import dev.crepe.domain.channel.actor.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User API", description = "유저 계정 관련 API")
public class UserController {

    private final UserService userService;


    @PostMapping("/signup")
    @Operation(summary = "유저 회원가입", description = "일반 유저 회원가입")
    public ResponseEntity<String>  signup(@Valid @RequestBody UserSignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }



    @PatchMapping("/change/nickname")
    @UserAuth
    @Operation(summary = "닉네임 변경", description = "일반유저의 닉네임을 변경합니다.")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<String> changeNickname(@Valid @RequestBody ChangeNicknameRequest request, AppAuthentication auth) {
        userService.changeNickname(request, auth.getUserEmail());
        return ResponseEntity.ok("닉네임 변경 성공");
    }

    @GetMapping("/my")
    @UserAuth
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    public ResponseEntity<UserInfoResponse> getMyInfo(AppAuthentication auth) {
        UserInfoResponse response = userService.getUserInfo(auth.getUserEmail());
        return ResponseEntity.ok(response);
    }

}
