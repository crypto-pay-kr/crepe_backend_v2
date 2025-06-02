package dev.crepe.infra.sms.service;

import dev.crepe.infra.sms.model.InMemorySmsAuthService;
import dev.crepe.infra.sms.model.SmsType;
import dev.crepe.infra.sms.model.dto.request.SendSmsCodeRequest;

public interface SmsManageService {

    void sendSmsCode(SendSmsCodeRequest request);

    void verifySmsCode(String phoneNumber, String code, SmsType smsType);

    InMemorySmsAuthService.SmsAuthData getSmsAuthData(String phoneNumber, SmsType smsType);


    void sendSmsCodeWithAuth(SendSmsCodeRequest request, String email);
}