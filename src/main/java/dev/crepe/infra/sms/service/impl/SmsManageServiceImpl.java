package dev.crepe.infra.sms.service.impl;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.infra.sms.model.InMemorySmsAuthService;
import dev.crepe.infra.sms.model.SmsType;
import dev.crepe.infra.sms.model.dto.request.SendSmsCodeRequest;
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
    public void sendSmsCode(SendSmsCodeRequest request) {
        log.info("SMS 인증 코드 전송 요청. 전화번호: {}, 타입: {}", request.getPhone(), request.getSmsType());

        // SIGN_UP일 경우 번호 중복 확인
        if (request.getSmsType() == SmsType.SIGN_UP && actorRepository.existsByPhoneNum(request.getPhone())) {
            throw exceptionDbService.getException("ACTOR_009");
        }

        String code = CodeGenerator.generateCode(digitLength);

        // 메모리 저장소에 인증 번호 저장
        inMemorySmsAuthService.saveAuthCode(request.getPhone(), code, request.getSmsType());

        // SMS 전송
        nhnSmsService.sendSms(request.getPhone(), code);
        log.info("SMS 전송 완료 - 전화번호: {}, 코드: {}", request.getPhone(), code);
    }

    @Override
    public void sendSmsCodeWithAuth(SendSmsCodeRequest request, String email) {
        log.info("상품가입용 SMS 인증 코드 전송 요청. 전화번호: {}, 타입: {}", request.getPhone(), request.getSmsType(), email);

        Actor actor = actorRepository.findByEmail(email)
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_001"));

        if (!(actor.getPhoneNum()).equals(request.getPhone())) {
            throw exceptionDbService.getException("ACTOR_010"); // 인증 실패 예외
        }

        String code = CodeGenerator.generateCode(digitLength);

        // 메모리 저장소에 인증 번호 저장
        inMemorySmsAuthService.saveAuthCode(request.getPhone(), code, request.getSmsType());

        // SMS 전송
        nhnSmsService.sendSms(request.getPhone(), code);
        log.info("SMS 전송 완료 - 전화번호: {}, 코드: {}", request.getPhone(), code);
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