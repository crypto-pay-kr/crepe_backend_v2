package dev.crepe.global.config;

import dev.crepe.global.util.AESUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

    @Value("${app.encryption.secret-key}")
    private String secretKey;

    @Bean
    public AESUtil aesUtil() {
        return new AESUtil(secretKey);
    }

}
