package dev.crepe.infra.sms.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class InMemorySmsAuthService {

    private static final int EXPIRATION_SECONDS = 180; // 인증 번호 유효 시간 (3분)
    private final ConcurrentHashMap<String, SmsAuthData> authStore = new ConcurrentHashMap<>();

    public void saveAuthCode(String phoneNumber, String code, SmsType type) {
        authStore.put(phoneNumber, new SmsAuthData(code, type, LocalDateTime.now().plusSeconds(EXPIRATION_SECONDS), phoneNumber));
    }
    public SmsAuthData getAuthData(String phone) {
        return authStore.get(phone);
    }

    public boolean verifyAuthCode(String phone, String code) {
        SmsAuthData authData = authStore.get(phone);
        if (authData == null) {
            return false;
        }
        if (authData.getExpiration().isBefore(LocalDateTime.now())) {
            authStore.remove(phone); // 만료된 데이터 제거
            return false;
        }
        return authData.getCode().equals(code);
    }

    @Getter
    public static class SmsAuthData {
        private final String code;
        private final String phoneNumber;
        private final SmsType smsType;
        private final LocalDateTime expiration;

        public SmsAuthData(String code, SmsType smsType, LocalDateTime expiration, String phoneNumber) {
            this.code = code;
            this.smsType = smsType;
            this.expiration = expiration;
            this.phoneNumber = phoneNumber; // 초기화
        }
    }
}
