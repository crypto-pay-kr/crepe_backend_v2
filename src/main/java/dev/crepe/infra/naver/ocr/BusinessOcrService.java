package dev.crepe.infra.naver.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class BusinessOcrService {

    @Value("${naver.cloud.business-ocr.secret-key}")
    private String secretKey;
    @Value("${naver.cloud.business-ocr.base-url}")
    private String baseUrl;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String processMultipartImage(MultipartFile file) throws IOException {
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

        // 메시지 본문 JSON
         String messageJson = String.format(
                "{\"version\":\"V2\",\"requestId\":\"%s\",\"timestamp\":%d,\"images\":[{\"name\":\"%s\",\"format\":\"jpg\"}]}",
                UUID.randomUUID(), System.currentTimeMillis(), file.getOriginalFilename()
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

    private String parseResponseData(String response) throws IOException {
        JsonNode root = objectMapper.readTree(response);
        StringBuilder result = new StringBuilder();

        System.out.println("OCR Raw Response: " + response);

        JsonNode images = root.path("images");
        if (images.isArray() && images.size() > 0) {
            JsonNode image = images.get(0);

            JsonNode bizLicense = image.path("bizLicense").path("result");
            if (!bizLicense.isMissingNode()) {

                append(result, "사업자등록번호: ", bizLicense, "registerNumber");
                append(result, "상호: ", bizLicense, "corpName");
                append(result, "대표자: ", bizLicense, "repName");
                append(result, "개업일자: ", bizLicense, "openDate");
                append(result, "사업장소재지: ", bizLicense, "bisAddress");
                append(result, "업태: ", bizLicense, "bisType");
                append(result, "종목: ", bizLicense, "bisItem");

            }
        }

        return result.toString().trim();
    }

    private void append(StringBuilder builder, String label, JsonNode parent, String fieldName) {
        JsonNode arr = parent.path(fieldName);
        if (arr.isArray() && arr.size() > 0) {
            String text = arr.get(0).path("text").asText(null);
            if (text != null && !text.isBlank()) {
                builder.append(label).append(text).append("\n");
            }
        }
    }

}