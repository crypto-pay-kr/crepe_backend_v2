package dev.crepe.domain.core.util.upbit.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.core.transfer.model.dto.response.GetDepositResponse;
import dev.crepe.global.config.UpbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UpbitDepositService {

    private final UpbitConfig upbitConfig;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public UpbitDepositService(UpbitConfig upbitConfig, ObjectMapper objectMapper, WebClient.Builder webClientBuilder) {
        this.upbitConfig = upbitConfig;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder
                .baseUrl(upbitConfig.getApiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // 입금 내역 조회
    public List<GetDepositResponse> getDepositListById(String currency, String txid) {
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("currency", currency);
            params.put("txid", txid);

            String queryString = buildQueryString(params);
            String queryHash = hash(queryString);
            String jwtToken = createJwtToken(queryHash);

            String url = "/v1/deposit?" + queryString;

            String responseBody = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // 동기 호출

            GetDepositResponse deposit = objectMapper.readValue(responseBody, GetDepositResponse.class);
            return List.of(deposit);
        } catch (Exception e) {
            log.error("입금 리스트 조회 실패", e);
            return Collections.emptyList();
        }
    }

    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private String hash(String queryString) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes(StandardCharsets.UTF_8));
        return String.format("%0128x", new BigInteger(1, md.digest()));
    }

    private String createJwtToken(String queryHash) {
        Algorithm algorithm = Algorithm.HMAC256(upbitConfig.getSecretKey());
        return JWT.create()
                .withClaim("access_key", upbitConfig.getAccessKey())
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);
    }
}