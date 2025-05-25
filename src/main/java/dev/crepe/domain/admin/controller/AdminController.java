package dev.crepe.domain.admin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.admin.service.AdminService;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.service.ActorService;
import dev.crepe.infra.naver.captcha.service.NaverCaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final NaverCaptchaService captchaService;
    private final ActorService actorService;

    @PostMapping("/login")
    @Operation(summary = "관리자 로그인", description = "관리자 로그인")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest request) {
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
            } else {
                // 관리자 로그인은 캡차 필수
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "보안을 위해 캡차 인증이 필요합니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }


            TokenResponse tokenResponse = adminService.adminLogin(request).getData();
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }





}
