package dev.crepe.domain.bank.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.BankAuth;
import dev.crepe.domain.bank.model.dto.request.ChangeBankCIRequest;
import dev.crepe.domain.bank.model.dto.request.ChangeBankPhoneRequest;
import dev.crepe.domain.bank.model.dto.response.GetBankInfoDetailResponse;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.channel.actor.exception.LoginFailedException;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;

import dev.crepe.infra.naver.captcha.service.NaverCaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bank")
@Slf4j
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final NaverCaptchaService captchaService;


    @PostMapping("/login")
    @Operation(summary = "은행 로그인", description = "은행 로그인")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 캡차 검증
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

            // BankService 호출 (Service에서 중복 로그인 방지 처리)
            TokenResponse tokenResponse = bankService.login(request).getData();
            return ResponseEntity.ok(tokenResponse);

        } catch (LoginFailedException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "로그인 실패");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "은행 정보 조회", description = "은행 정보를 조회합니다..")
    @GetMapping()
    @BankAuth
    @SecurityRequirement(name="bearer-jwt")
    public ResponseEntity<GetBankInfoDetailResponse> getBankInfoDetail(AppAuthentication auth) {
        GetBankInfoDetailResponse res = bankService.getBankAllDetails(auth.getUserEmail());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }


    @Operation(summary = "담당자 연결 번호 변경", description = "은행 내 담당자 연결 번호를 변경합니다.")
    @PatchMapping("/change/phone")
    @BankAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<String> changePhone(@Valid @RequestBody ChangeBankPhoneRequest request, AppAuthentication auth) {
        bankService.changePhone(request, auth.getUserEmail());
        return ResponseEntity.ok("담당자 연결 번호 변경 성공");
    }


    @Operation(summary = "은행 CI 이미지 변경", description = "은행의 CI 이미지를 변경합니다.")
    @PatchMapping("/change/ci")
    @BankAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<String> changeBankCI(@Valid @RequestParam("ciImage") MultipartFile ciImage, AppAuthentication auth) {
        bankService.changeBankCI(ciImage, auth.getUserEmail());
        return ResponseEntity.ok("은행 CI 이미지 변경 성공");
    }




}
