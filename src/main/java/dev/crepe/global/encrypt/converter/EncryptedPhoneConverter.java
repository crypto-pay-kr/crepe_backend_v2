package dev.crepe.global.encrypt.converter;

import dev.crepe.global.encrypt.provider.ApplicationContextProvider;
import dev.crepe.global.util.AESUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

@Slf4j
@Converter
public class EncryptedPhoneConverter implements AttributeConverter<String, String> {

    private AESUtil aesUtil;

    private AESUtil getAESUtil() {
        if (aesUtil == null) {
            try {
                aesUtil = ApplicationContextProvider.getBean(AESUtil.class);
                log.info("AESUtil 빈 로드 성공 (Phone)");
            } catch (Exception e) {
                log.error("AESUtil 빈을 가져올 수 없습니다 (Phone)", e);
                throw new RuntimeException("암호화 유틸리티를 초기화할 수 없습니다", e);
            }
        }
        return aesUtil;
    }

    @Override
    public String convertToDatabaseColumn(String phoneNumber) {
        log.info("=== Phone convertToDatabaseColumn 실행 ===");
        log.info("저장할 전화번호: {}", phoneNumber);

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            log.info("빈 전화번호, 그대로 반환");
            return phoneNumber;
        }

        if (isAlreadyEncrypted(phoneNumber)) {
            log.info("이미 암호화된 전화번호로 판단, 그대로 반환");
            return phoneNumber;
        }

        try {
            String encrypted = getAESUtil().encrypt(phoneNumber);
            log.info("전화번호 암호화 성공: {} -> {}", phoneNumber, encrypted);
            return encrypted;
        } catch (Exception e) {
            log.error("휴대폰 번호 암호화 실패: {}", phoneNumber, e);
            throw new RuntimeException("휴대폰 번호 암호화에 실패했습니다", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        log.info("=== Phone convertToEntityAttribute 실행 ===");
        log.info("DB에서 가져온 전화번호: {}", dbData);

        if (dbData == null || dbData.trim().isEmpty()) {
            log.info("빈 전화번호 데이터, 그대로 반환");
            return dbData;
        }

        if (!isAlreadyEncrypted(dbData)) {
            log.info("평문 전화번호로 판단, 그대로 반환");
            return dbData;
        }

        try {
            String decrypted = getAESUtil().decrypt(dbData);
            log.info("전화번호 복호화 성공: {} -> {}", dbData, decrypted);
            return decrypted;
        } catch (Exception e) {
            log.error("휴대폰 번호 복호화 실패, 원본 데이터 반환: {}", dbData, e);
            return dbData; // 복호화 실패 시 원본 반환
        }
    }

    private boolean isPhoneNumberFormat(String data) {
        if (data == null || data.trim().isEmpty()) {
            return false;
        }

        boolean isPhone = data.matches("^01[016789][0-9]{7,8}$");
        log.debug("전화번호 형식 확인: {} -> {}", data, isPhone);
        return isPhone;
    }

    private boolean isAlreadyEncrypted(String data) {
        // null 체크
        if (data == null || data.trim().isEmpty()) {
            return false;
        }

        // 휴대폰 번호 형식이면 암호화되지 않은 것
        if (isPhoneNumberFormat(data)) {
            log.debug("전화번호 패턴 매치, 평문으로 판단: {}", data);
            return false;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(data);
            boolean isEncrypted = decoded.length >= 16 &&
                    data.length() > 15 &&
                    !data.matches("^[0-9]+$");

            log.debug("전화번호 암호화 데이터 판단 결과: {} for data: {}", isEncrypted, data);
            return isEncrypted;
        } catch (IllegalArgumentException e) {
            // Base64 디코딩 실패 = 암호화되지 않은 일반 텍스트
            log.debug("Base64 디코딩 실패, 평문으로 판단: {}", data);
            return false;
        }
    }
}