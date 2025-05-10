package dev.crepe.domain.core.util.upbit.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpbitExchangeService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final String URL_TEMPLATE = "/v1/ticker?markets=KRW-%s";

    public BigDecimal getLatestRate(String currency) {
        try {
            String uri = String.format(URL_TEMPLATE, currency.toUpperCase());

            String responseBody = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // 동기 호출

            JsonNode root = objectMapper.readTree(responseBody);
            BigDecimal tradePrice = root.get(0).get("trade_price").decimalValue();

            log.info("Fetched exchange rate for {}: {}", currency, tradePrice);
            return tradePrice;

        } catch (Exception e) {
            log.error("시세 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("시세 조회 중 오류 발생");
        }
    }
}