package dev.crepe.infra.sms.service.impl;

import dev.crepe.infra.sms.exception.SmsAuthCodeNotValidException;
import dev.crepe.infra.sms.exception.SmsAuthNotFoundException;
import dev.crepe.infra.sms.model.InMemorySmsAuthService;
import dev.crepe.infra.sms.model.SmsType;
import dev.crepe.infra.sms.service.NhnSmsService;
import dev.crepe.infra.sms.service.SmsManageService;
import dev.crepe.infra.sms.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsManageServiceImpl implements SmsManageService {

    private static final int digitLength = 6;

    private final NhnSmsService nhnSmsService;
    private final InMemorySmsAuthService inMemorySmsAuthService;

    @Override
    public InMemorySmsAuthService.SmsAuthData getSmsAuthData(String phoneNumber, SmsType smsType) {
        InMemorySmsAuthService.SmsAuthData authData = inMemorySmsAuthService.getAuthData(phoneNumber);

        if (authData == null || !authData.getSmsType().equals(smsType)) {
            throw new SmsAuthNotFoundException();
        }

        return authData;
    }

    @Override
    public void sendSmsCode(String phoneNumber, SmsType type) {
        String code = CodeGenerator.generateCode(digitLength);

        // 메모리 저장소에 인증 번호 저장
        inMemorySmsAuthService.saveAuthCode(phoneNumber, code, type);

        // SMS 전송
        nhnSmsService.sendSms(phoneNumber,  code);
    }

    @Override
    public void verifySmsCode(String phoneNumber, String code, SmsType smsType) {

        // 메모리 저장소에서 인증 번호 검증
        InMemorySmsAuthService.SmsAuthData authData = inMemorySmsAuthService.getAuthData(phoneNumber);
        if (authData == null || !authData.getCode().equals(code) || !authData.getSmsType().equals(smsType)) {
            throw new SmsAuthCodeNotValidException();
        }
    }

}