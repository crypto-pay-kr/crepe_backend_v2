package dev.crepe.infra.sms.controller;

import dev.crepe.infra.sms.model.dto.request.SendSmsCodeRequest;
import dev.crepe.infra.sms.model.dto.request.VerifySmsCodeRequest;
import dev.crepe.infra.sms.service.SmsManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
@Slf4j
public class SmsController {

    private final SmsManageService smsManageService;

    // SMS 인증 코드 전송
    @PostMapping("/code")
    public ResponseEntity<String> sendSmsCode(@Valid @RequestBody SendSmsCodeRequest request) {
        smsManageService.sendSmsCode(request.getPhone(), request.getSmsType());
        return ResponseEntity.ok("SMS 인증 코드 전송 성공");
    }

    // SMS 인증 코드 확인
    @PostMapping("/verify")
    public ResponseEntity<String> verifySmsCode(@Valid @RequestBody VerifySmsCodeRequest request) {
        smsManageService.verifySmsCode(request.getPhone(), request.getCode(), request.getSmsType());
        return  ResponseEntity.ok("SMS 인증 코드 확인 성공");
    }
}
