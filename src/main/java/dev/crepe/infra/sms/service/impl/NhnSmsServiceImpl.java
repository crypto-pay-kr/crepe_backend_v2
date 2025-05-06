package dev.crepe.infra.sms.service.impl;

import dev.crepe.infra.sms.exception.CannotSendSmsException;
import dev.crepe.infra.sms.exception.NotValidSmsResponseException;
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

    @Value("${nhn.sms.api-path}")
    private String apiPath;

    @Value("${nhn.sms.secret-key}")
    private String secretKey;

    @Value("${nhn.sms.sender-phone}")
    private String senderPhone;

    private final String baseUrl;

    public NhnSmsServiceImpl(WebClient.Builder webClientBuilder, @Value("${nhn.sms.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder.baseUrl(this.baseUrl).build();
    }

    @Override
    public void sendSms(String phone, String smsVerificationCode) {
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
                log.error("SMS 전송 실패. 응답: {}", response);
                throw new CannotSendSmsException();
            }

            log.info("SMS가 성공적으로 전송되었습니다. 수신자 번호: {}", phone);
        } catch (CannotSendSmsException e) {
            throw e; // 이미 처리된 예외는 그대로 던짐
        } catch (Exception e) {
            log.error("SMS 전송 중 오류 발생. 수신자 번호: {}, 오류: {}", phone, e.getMessage(), e);
            throw new NotValidSmsResponseException();
        }
    }
}