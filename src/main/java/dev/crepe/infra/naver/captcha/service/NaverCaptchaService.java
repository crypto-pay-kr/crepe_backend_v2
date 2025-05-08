package dev.crepe.infra.naver.captcha.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Service
public class NaverCaptchaService {

    @Value("${naver.cloud.captcha.client-id}")
    private String clientId;

    @Value("${naver.cloud.captcha.client-secret}")
    private String clientSecret;

    private static final String NAVER_CAPTCHA_API_URL = "https://naveropenapi.apigw.ntruss.com/captcha/v1/nkey";

    public String generateCaptchaKey() throws Exception {
        return callNaverCaptchaApi("0", null, null);
    }

    public String validateCaptchaValue(String key, String value) throws Exception {
        return callNaverCaptchaApi("1", key, value);
    }

    private String callNaverCaptchaApi(String code, String key, String value) throws Exception {
        StringBuilder apiUrlBuilder = new StringBuilder(NAVER_CAPTCHA_API_URL);
        apiUrlBuilder.append("?code=").append(code);

        if (key != null && !key.isEmpty()) {
            apiUrlBuilder.append("&key=").append(key);
        }

        if (value != null && !value.isEmpty()) {
            apiUrlBuilder.append("&value=").append(value);
        }

        BufferedReader br = getBufferedReader(apiUrlBuilder);

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }

        br.close();
        return response.toString();
    }

    private BufferedReader getBufferedReader(StringBuilder apiUrlBuilder) throws IOException {
        URL url = new URL(apiUrlBuilder.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        log.info("API URL: {}", apiUrlBuilder);
        log.info("Client ID 설정 여부: {}", clientId != null && !clientId.isEmpty() ? "설정됨" : "미설정");
        log.info("Client Secret 설정 여부: {}", clientSecret != null && !clientSecret.isEmpty() ? "설정됨" : "미설정");

        con.setRequestMethod("GET");
        con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
        con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);

        int responseCode = con.getResponseCode();
        BufferedReader br;

        if (responseCode == 200) { // 정상 호출
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {  // 오류 발생
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
        return br;
    }
}
