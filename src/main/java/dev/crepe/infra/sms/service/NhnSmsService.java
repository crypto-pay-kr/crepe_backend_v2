package dev.crepe.infra.sms.service;

public interface NhnSmsService {

    void sendSms(String phone, String smsVerificationCode);

}