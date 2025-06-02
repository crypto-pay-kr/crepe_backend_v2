package dev.crepe.domain.admin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.admin.service.AdminService;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.infra.naver.captcha.service.NaverCaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final NaverCaptchaService captchaService;
    private final ExceptionDbService exceptionDbService;

    @PostMapping("/login")
    @Operation(summary = "관리자 로그인", description = "관리자 로그인")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest request) {
        try {
            // 관리자 로그인은 캡차 필수
            if (request.getCaptchaKey() == null || request.getCaptchaKey().isEmpty()) {
                throw exceptionDbService.getException("CAPTCHA_002");
            }

            String validationResult = captchaService.validateCaptchaValue(
                    request.getCaptchaKey(),
                    request.getCaptchaValue()
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(validationResult);
            boolean isValid = rootNode.path("result").asBoolean();

            if (!isValid) {
                throw exceptionDbService.getException("CAPTCHA_001");
            }

            TokenResponse tokenResponse = adminService.adminLogin(request).getData();
            return ResponseEntity.ok(tokenResponse);

        } catch (Exception e) {
            throw exceptionDbService.getException("CAPTCHA_003");
        }


    }


}
