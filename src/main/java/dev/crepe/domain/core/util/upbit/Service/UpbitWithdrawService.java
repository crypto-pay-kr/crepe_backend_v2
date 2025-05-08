package dev.crepe.domain.core.util.upbit.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;
import dev.crepe.domain.core.transfer.model.dto.response.CheckWithdrawResponse;
import dev.crepe.domain.core.transfer.model.dto.response.GetWithdrawResponse;
import dev.crepe.global.config.UpbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UpbitWithdrawService {

    private final UpbitConfig upbitConfig;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public UpbitWithdrawService(UpbitConfig upbitConfig, ObjectMapper objectMapper, WebClient.Builder builder) {
        this.upbitConfig = upbitConfig;
        this.objectMapper = objectMapper;
        this.webClient = builder
                .baseUrl(upbitConfig.getApiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public GetWithdrawResponse requestWithdraw(GetWithdrawRequest request, String address, String tag, String netType) {
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("currency", request.getCurrency());
            params.put("net_type", netType);
            params.put("amount", request.getAmount());
            params.put("address", address);
            if (tag != null && !tag.isBlank()) {
                params.put("secondary_address", tag);
            }
            params.put("transaction_type", "internal");

            String queryString = buildQueryString(params);
            String queryHash = hash(queryString);
            String jwtToken = createJwtToken(queryHash);

            String responseBody = webClient.post()
                    .uri("/v1/withdraws/coin")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(params))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(responseBody, GetWithdrawResponse.class);

        } catch (Exception e) {
            log.error("출금 요청 실패", e);
            return null;
        }
    }

    public CheckWithdrawResponse checkSettlement(String uuid) {
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("uuid", uuid);

            String queryString = buildQueryString(params);
            String queryHash = hash(queryString);
            String jwtToken = createJwtToken(queryHash);

            String responseBody = webClient.get()
                    .uri("/v1/withdraw?" + queryString)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Map<String, Object> result = objectMapper.readValue(responseBody, new TypeReference<>() {});
            String state = (String) result.get("state");
            String amountStr = (String) result.get("amount");
            String doneAt = (String) result.get("done_at");

            boolean completed = "DONE".equalsIgnoreCase(state);
            return new CheckWithdrawResponse(
                    completed,
                    new BigDecimal(amountStr),
                    doneAt
            );

        } catch (Exception e) {
            throw new RuntimeException("단건 출금 조회 실패", e);
        }
    }

    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
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