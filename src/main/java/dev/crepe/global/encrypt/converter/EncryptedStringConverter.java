package dev.crepe.global.encrypt.converter;

import dev.crepe.global.encrypt.provider.ApplicationContextProvider;
import dev.crepe.global.util.AESUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

@Slf4j
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private AESUtil aesUtil;

    private AESUtil getAESUtil() {
        if (aesUtil == null) {
            try {
                aesUtil = ApplicationContextProvider.getBean(AESUtil.class);
                log.info("AESUtil 빈 로드 성공");
            } catch (Exception e) {
                log.error("AESUtil 빈을 가져올 수 없습니다", e);
                throw new RuntimeException("암호화 유틸리티를 초기화할 수 없습니다", e);
            }
        }
        return aesUtil;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        log.info("=== convertToDatabaseColumn 실행 ===");
        log.info("저장할 데이터: {}", attribute);

        if (attribute == null || attribute.trim().isEmpty()) {
            log.info("빈 데이터, 그대로 반환");
            return attribute;
        }

        // 이미 암호화된 데이터인지 확인
        if (isAlreadyEncrypted(attribute)) {
            log.info("이미 암호화된 데이터로 판단, 그대로 반환");
            return attribute;
        }

        try {
            String encrypted = getAESUtil().encrypt(attribute);
            log.info("암호화 성공: {} -> {}", attribute, encrypted);
            return encrypted;
        } catch (Exception e) {
            log.error("문자열 암호화 실패: {}", attribute, e);
            throw new RuntimeException("문자열 암호화에 실패했습니다", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        log.info("=== convertToEntityAttribute 실행 ===");
        log.info("DB에서 가져온 데이터: {}", dbData);

        if (dbData == null || dbData.trim().isEmpty()) {
            log.info("빈 데이터, 그대로 반환");
            return dbData;
        }

        if (!isAlreadyEncrypted(dbData)) {
            log.info("평문 데이터로 판단, 그대로 반환: {}", dbData);
            return dbData;
        }

        try {
            String decrypted = getAESUtil().decrypt(dbData);
            log.info("복호화 성공: {} -> {}", dbData, decrypted);
            return decrypted;
        } catch (Exception e) {
            log.error("문자열 복호화 실패, 원본 데이터 반환: {}", dbData, e);
            return dbData; // 복호화 실패 시 원본 데이터 반환
        }
    }

    private boolean isAlreadyEncrypted(String data) {
        if (data == null || data.trim().isEmpty()) {
            return false;
        }

        // 한글 이름이나 일반 텍스트면 평문으로 판단
        if (data.matches("^[가-힣]{2,5}$") || data.matches("^[a-zA-Z\\s]{2,20}$")) {
            log.debug("일반 텍스트로 판단: {}", data);
            return false;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(data);

            // 암호화된 데이터의 특징:
            // 1. Base64 디코딩이 가능하고
            // 2. 디코딩된 바이트 길이가 16바이트 이상이고 (AES 블록 크기)
            // 3. 원본 문자열 길이가 충분히 길고
            boolean isEncrypted = decoded.length >= 16 && data.length() > 20;

            log.info("암호화 데이터 판단 결과: {} for data: {} (decoded length: {}, data length: {})",
                    isEncrypted, data, decoded.length, data.length());
            return isEncrypted;
        } catch (IllegalArgumentException e) {
            log.debug("Base64 디코딩 실패, 평문으로 판단: {}", data);
            return false;
        }
    }

    private boolean hasReadablePattern(String data) {
        // 연속된 같은 문자 3개 이상이 있으면 일반 텍스트일 가능성
        if (data.matches(".*(..)\\1+.*")) {
            return true;
        }

        return data.toLowerCase().matches(".*[aeiou].*[bcdfghjklmnpqrstvwxyz].*");
    }
}