package dev.crepe.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private final SecretKeySpec secretKey;

    public AESUtil(@Value("${app.encryption.secret-key}") String key) {
        // 키 길이 검증 (AES-256은 32바이트 필요)
        if (key == null || key.getBytes().length != 32) {
            throw new IllegalArgumentException("AES-256은 32바이트 키가 필요합니다. 현재: " +
                    (key != null ? key.getBytes().length : 0) + "바이트");
        }
        this.secretKey = new SecretKeySpec(key.getBytes(), "AES");
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV + 암호화된 데이터를 합쳐서 Base64 인코딩
            byte[] encryptedWithIv = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(cipherText, 0, encryptedWithIv, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            throw new RuntimeException("암호화 실패", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

            // IV와 암호화된 데이터 분리
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[decodedBytes.length - GCM_IV_LENGTH];
            System.arraycopy(decodedBytes, 0, iv, 0, iv.length);
            System.arraycopy(decodedBytes, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("복호화 실패", e);
        }
    }
}
