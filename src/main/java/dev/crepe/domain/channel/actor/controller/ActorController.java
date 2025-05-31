package dev.crepe.domain.channel.actor.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.channel.actor.model.dto.request.*;
import dev.crepe.domain.channel.actor.model.dto.response.GetFinancialSummaryResponse;
import dev.crepe.infra.naver.ocr.id.entity.dto.IdCardOcrResponse;
import dev.crepe.infra.naver.ocr.id.service.IdCardOcrService;
import dev.crepe.infra.otp.model.dto.OtpSetupResponse;
import dev.crepe.infra.otp.model.entity.OtpCredential;
import dev.crepe.infra.otp.service.OtpService;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.service.ActorService;
import dev.crepe.global.model.dto.ApiResponse;
import dev.crepe.infra.naver.captcha.service.NaverCaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ActorController {

    private final ActorService actorService;
    private final NaverCaptchaService captchaService;
    private final OtpService otpService;
    private final IdCardOcrService idCardOcrService;


    // 이메일 중복 확인
    @PostMapping("/check/email-duplicate")
    @Operation(summary = "회원가입 시 이메일 중복 체크", description = "이메일 중복 체크")
    public ResponseEntity<Void> checkEmailDuplicate(@RequestBody Map<String, String> request) {
        if (actorService.isEmailExists(request.get("email"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().build();
    }




    // 닉네임 중복 확인
    @PostMapping("/check/nickname-duplicate")
    public ResponseEntity<Void> checkNicknameDuplicate(@RequestBody Map<String, String> request) {
        if (actorService.isNicknameExists(request.get("nickname"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().build();
    }

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
                errorResponse.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
    }

    @PostMapping("/otp/setup")
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

    @PostMapping("/otp/verify")
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

    @PostMapping("/otp/status")
    @Operation(summary = "OTP 상태 조회", description = "사용자의 OTP 설정 상태 조회")
    public ResponseEntity<?> getOtpStatus(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "이메일이 필요합니다");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ApiResponse<OtpCredential> response = otpService.getOtpStatus(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    @PostMapping("/otp/disable")
    @Operation(summary = "OTP 해제", description = "사용자의 OTP 설정을 완전히 삭제합니다")
    public ResponseEntity<?> disableOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "이메일이 필요합니다");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("OTP 해제 요청 - 이메일: {}", email);

            ApiResponse<Boolean> response = otpService.deleteOtp(email);

            if ("success".equals(response.getStatus())) {
                log.info("OTP 해제 성공 - 이메일: {}", email);
                return ResponseEntity.ok(response);
            } else {
                log.warn("OTP 해제 실패 - 이메일: {}, 메시지: {}", email, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("OTP 해제 중 예외 발생 - 오류: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "OTP 해제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
    @Operation(summary = "직업 등록", description = "상품 가입 전 직업 등록")
    @PostMapping("/add/occupation")
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<String> addOccupation(@RequestBody AddOccupationRequest request, AppAuthentication auth) {
        actorService.addOccupationName(request, auth.getUserEmail());
        return new ResponseEntity<>("직업 추가 완료",HttpStatus.OK);
    }

    // 소득 조회 api
    @Operation(summary = "소득 조회 및 등록", description = "소득을 조회하고 db에 등록")
    @PostMapping("/check/income")
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<GetFinancialSummaryResponse> checkActorIncome(AppAuthentication auth) {
        GetFinancialSummaryResponse res = actorService.checkIncome(auth.getUserEmail());
        return ResponseEntity.ok(res);
    }


    @Operation(summary = "신분증 ocr", description = "ocr 인증 후 나이, 성별 저장")
    @PostMapping(value = "/ocr/id", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<IdCardOcrResponse> processIdentityCardAndUpdateActor(
            @RequestParam("file") MultipartFile file, AppAuthentication auth) {

        log.info("OCR API 호출 시작");
        log.info("파일 정보 - 이름: {}, 크기: {}, 타입: {}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            // 인증 정보 확인
            String userEmail = auth.getUserEmail();
            log.info("요청한 사용자 이메일: {}", userEmail);

            if (file.isEmpty()) {
                log.warn("업로드된 파일이 비어있음");
                return ResponseEntity.badRequest().build();
            }

            log.info("OCR 처리 시작");
            // OCR 처리
            IdCardOcrResponse response = idCardOcrService.recognizeIdentityCard(file);
            log.info("OCR 처리 완료: {}", response);

            log.info("Actor 정보 업데이트 시작");
            // Actor 정보 업데이트
            actorService.updateFromIdCard(userEmail, response);
            log.info("Actor 정보 업데이트 완료");

            log.info("OCR API 성공적으로 완료");
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            log.error("엔티티를 찾을 수 없음: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("잘못된 인수: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("IO 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
