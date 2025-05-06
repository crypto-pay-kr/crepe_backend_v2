package dev.crepe.global.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "upbit")
public class UpbitConfig {

    private String accessKey;
    private String secretKey;
    private String apiUrl;

}

