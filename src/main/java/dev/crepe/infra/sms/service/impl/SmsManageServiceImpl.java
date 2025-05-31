package dev.crepe.infra.sms.service.impl;

import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
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
    private final ActorRepository actorRepository;
    private final ExceptionDbService exceptionDbService;

    @Override
    public InMemorySmsAuthService.SmsAuthData getSmsAuthData(String phoneNumber, SmsType smsType) {
        InMemorySmsAuthService.SmsAuthData authData = inMemorySmsAuthService.getAuthData(phoneNumber);

        if (authData == null || !authData.getSmsType().equals(smsType)) {
            throw exceptionDbService.getException("SMS_004");
        }

        return authData;
    }

    @Override
    public void sendSmsCode(String phoneNumber, SmsType type) {
        log.info("SMS 인증 코드 전송 요청. 전화번호: {}, 타입: {}", phoneNumber, type);

        // SIGN_UP일 경우 번호 중복 확인
        if (type == SmsType.SIGN_UP && actorRepository.existsByPhoneNum(phoneNumber)) {
            throw exceptionDbService.getException("ACTOR_009");
        }

        String code = CodeGenerator.generateCode(digitLength);

        // 메모리 저장소에 인증 번호 저장
        inMemorySmsAuthService.saveAuthCode(phoneNumber, code, type);

        // SMS 전송
        nhnSmsService.sendSms(phoneNumber,  code);
        log.info("SMS 전송 완료 - 전화번호: {}, 코드: {}", phoneNumber, code);
    }

    @Override
    public void verifySmsCode(String phoneNumber, String code, SmsType smsType) {
        log.info("SMS 인증 검증 호출 - 전화번호: {}, 코드: {}, SMS 타입: {}", phoneNumber, code, smsType);

        // 메모리 저장소에서 인증 번호 검증
        InMemorySmsAuthService.SmsAuthData authData = inMemorySmsAuthService.getAuthData(phoneNumber);
        if (authData == null || !authData.getCode().equals(code) || !authData.getSmsType().equals(smsType)) {
            throw exceptionDbService.getException("SMS_003");
        }

        log.info("SMS 인증 검증 성공 - 전화번호: {}, 코드: {}, SMS 타입: {}", phoneNumber, code, smsType);
    }

}