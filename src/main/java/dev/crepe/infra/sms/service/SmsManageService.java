package dev.crepe.infra.sms.service;

import dev.crepe.infra.sms.model.InMemorySmsAuthService;
import dev.crepe.infra.sms.model.SmsType;

public interface SmsManageService {

    void sendSmsCode(String phoneNumber, SmsType type);

    void verifySmsCode(String phoneNumber, String code, SmsType smsType);

    InMemorySmsAuthService.SmsAuthData getSmsAuthData(String phoneNumber, SmsType smsType);


}