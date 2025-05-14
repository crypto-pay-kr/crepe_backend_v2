package dev.crepe.global.config;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.ICredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OtpConfig {

    @Bean
    public GoogleAuthenticator googleAuthenticator() {
        return  new GoogleAuthenticator();
    }
}
