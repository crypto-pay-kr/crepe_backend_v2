package dev.crepe.domain.channel.actor.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.channel.actor.model.dto.request.*;
import dev.crepe.infra.otp.model.dto.OtpSetupResponse;
import dev.crepe.infra.otp.service.OtpService;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.service.ActorService;
import dev.crepe.global.model.dto.ApiResponse;
import dev.crepe.infra.naver.captcha.service.NaverCaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class ActorController {

    private final ActorService actorService;
    private final NaverCaptchaService captchaService;
    private final OtpService otpService;

    @GetMapping("/captcha")
    @Operation(summary = "로그인에 필요한 captcha 키 발급", description = "captcha 키 발급")
    public ResponseEntity<?> getCaptcha() {
        try {
            String captchaResponse = captchaService.generateCaptchaKey();
            log.info("captcha response: {}", captchaResponse);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(captchaResponse);
            String captchaKey = rootNode.path("key").asText();

            String captchaImageUrl = "https://naveropenapi.apigw.ntruss.com/captcha-bin/v1/ncaptcha?key=" + captchaKey;

            Map<String, String> response = new HashMap<>();
            response.put("captchaKey", captchaKey);
            response.put("captchaImageUrl", captchaImageUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "유저, 가맹점 로그인", description = "회원 로그인")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            if (request.getCaptchaKey() != null && !request.getCaptchaKey().isEmpty()) {
                String validationResult = captchaService.validateCaptchaValue(
                        request.getCaptchaKey(),
                        request.getCaptchaValue()
                );
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(validationResult);
                boolean isValid = rootNode.path("result").asBoolean();

                if (!isValid) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "캡차 검증에 실패했습니다. 다시 시도해주세요.");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }
                TokenResponse tokenResponse = actorService.login(request).getData();
                return ResponseEntity.ok(tokenResponse);
        }catch (Exception e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(errorResponse);
            }
    }

    @PostMapping("/setup")
    @Operation(summary = "OTP 설정", description = "사용자의 OTP 초기 설정")
    public ResponseEntity<?> setupOtp(@RequestParam String email) {
        try {
            ApiResponse<OtpSetupResponse> response = otpService.setupOtp(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/verify")
    @Operation(summary = "OTP 검증 및 활성화", description = "OTP 코드 검증 및 활성화")
    public ResponseEntity<?> verifyAndEnableOtp(@RequestParam String email, @RequestParam int otpCode) {
        try {
            ApiResponse<Boolean> response = otpService.verifyAndEnableOtp(email, otpCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
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

    // 직업 입력 받기
    @Operation(summary = "휴대폰 인증, 직업 등록", description = "상품 가입 전 휴대폰 인증 및 직업 등록")
    @PostMapping("/add/occupation")
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<String> addOccupation(@RequestBody AddOccupationRequest request, AppAuthentication auth) {
        actorService.addOccupationName(request, auth.getUserEmail());
        return new ResponseEntity<>("직업 추가 완료",HttpStatus.OK);
    }

    // 소득 조회 api
    @PostMapping("/check/income")
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<String> checkActorIncome(AppAuthentication auth) {
        actorService.checkIncome(auth.getUserEmail());
        return new ResponseEntity<>("소득 조회 성공",HttpStatus.OK);
    }

}
