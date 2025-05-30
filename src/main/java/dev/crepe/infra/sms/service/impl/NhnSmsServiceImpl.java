package dev.crepe.infra.sms.service.impl;

import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.infra.sms.model.dto.request.NhnSmsRequest;
import dev.crepe.infra.sms.model.dto.response.NhnSmsResponse;
import dev.crepe.infra.sms.service.NhnSmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class NhnSmsServiceImpl implements NhnSmsService {

    private final WebClient webClient;
    private final ExceptionDbService exceptionDbService;

    @Value("${nhn.sms.api-path}")
    private String apiPath;

    @Value("${nhn.sms.secret-key}")
    private String secretKey;

    @Value("${nhn.sms.sender-phone}")
    private String senderPhone;

    private final String baseUrl;


    public NhnSmsServiceImpl(WebClient.Builder webClientBuilder,
                             @Value("${nhn.sms.base-url}") String baseUrl,
                             ExceptionDbService exceptionDbService) {
        this.baseUrl = baseUrl;
        this.exceptionDbService = exceptionDbService;
        this.webClient = webClientBuilder.baseUrl(this.baseUrl).build();
    }


    @Override
    public void sendSms(String phone, String smsVerificationCode) {
        log.info("SMS 전송 요청. 수신자 번호: {}, 인증 코드: {}", phone, smsVerificationCode);
        // 공백 제거
        String trimmedApiPath = apiPath.trim();
        try {
            // 요청 DTO 생성
            NhnSmsRequest request = new NhnSmsRequest(senderPhone, phone, "인증 번호: " + smsVerificationCode);

            // 요청 전송 및 응답 처리
            NhnSmsResponse response = webClient.post()
                    .uri(trimmedApiPath)
                    .header("X-Secret-Key", secretKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(NhnSmsResponse.class)
                    .block();

            if (response == null || !response.getHeader().getIsSuccessful()) {
                throw exceptionDbService.getException("SMS_001");
            }

            log.info("SMS가 성공적으로 전송되었습니다. 수신자 번호: {}", phone);
        } catch  (IllegalArgumentException e)  {
            throw exceptionDbService.getException("SMS_002");
        } catch (Exception e) {
            throw exceptionDbService.getException("SMS_003");
        }
    }
}