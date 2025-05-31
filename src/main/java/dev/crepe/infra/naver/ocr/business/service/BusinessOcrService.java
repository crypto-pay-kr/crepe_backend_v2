package dev.crepe.infra.naver.ocr.business.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.infra.naver.ocr.business.dto.BusinessOcrResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessOcrService {

    @Value("${naver.cloud.business-ocr.secret-key}")
    private String secretKey;
    @Value("${naver.cloud.business-ocr.base-url}")
    private String baseUrl;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final ExceptionDbService exceptionDbService;

    public BusinessOcrResponse processMultipartImage(MultipartFile file) throws IOException {
        log.info("사업자 등록증 인식 시작 - 파일 이름: {}", file.getOriginalFilename());
        String boundary = UUID.randomUUID().toString();
        URL url = new URL(baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setReadTimeout(5000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("X-OCR-SECRET",secretKey);


        // 파일 확장자 확인
        String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
        if (!extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("png")) {
            throw exceptionDbService.getException("OCR_001"); // 지원하지 않는 파일 형식
        }
        // 메시지 본문 JSON
        String messageJson = String.format(
                "{\"version\":\"V2\",\"requestId\":\"%s\",\"timestamp\":%d,\"images\":[{\"name\":\"%s\",\"format\":\"%s\"}]}",
                UUID.randomUUID(), System.currentTimeMillis(), file.getOriginalFilename(), extension
        );

        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            // 메시지
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"message\"\r\n\r\n");
            out.writeBytes(messageJson + "\r\n");

            // 파일
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"\r\n");
            out.writeBytes("Content-Type: " + file.getContentType() + "\r\n\r\n");
            out.write(file.getBytes());
            out.writeBytes("\r\n");

            out.writeBytes("--" + boundary + "--\r\n");
            out.flush();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            return parseResponseData(response.toString());
        } finally {
            connection.disconnect();
        }
    }

    private BusinessOcrResponse parseResponseData(String response) throws IOException {
        JsonNode root = objectMapper.readTree(response);
        JsonNode images = root.path("images");

        if (!images.isArray() || images.isEmpty()) {
            throw exceptionDbService.getException("OCR_002");
        }

        JsonNode bizLicense = images.get(0).path("bizLicense").path("result");

        return BusinessOcrResponse.builder()
                .registerNumber(getText(bizLicense, "registerNumber"))
                .corpName(getText(bizLicense, "corpName"))
                .representativeName(getText(bizLicense, "repName"))
                .openDate(getText(bizLicense, "openDate"))
                .address(getText(bizLicense, "bisAddress"))
                .businessType(getText(bizLicense, "bisType"))
                .businessItem(getText(bizLicense, "bisItem"))
                .build();
    }

    private String getText(JsonNode parent, String fieldName) {
        JsonNode arr = parent.path(fieldName);
        if (arr.isArray() && arr.size() > 0) {
            String text = arr.get(0).path("text").asText();
            if (text != null && !text.isBlank()) {
                return text;
            }
        }
        return null;
    }
}