package dev.crepe.global.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@AllArgsConstructor
public class WebClientConfig {

    private final UpbitConfig upbitConfig;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(upbitConfig.getApiUrl())
                .build();
    }
}