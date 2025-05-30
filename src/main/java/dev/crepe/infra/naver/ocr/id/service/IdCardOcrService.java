package dev.crepe.infra.naver.ocr.id.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.infra.naver.ocr.id.entity.IdCardType;
import dev.crepe.infra.naver.ocr.id.entity.dto.IdCardOcrResponse;
import dev.crepe.infra.naver.ocr.id.util.IdCardOcrUtil;
import dev.crepe.infra.naver.ocr.id.util.MultipartInputStreamFileResource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class IdCardOcrService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExceptionDbService exceptionDbService;

    @Value("${naver.cloud.id-ocr.secret-key}") private String secretKey;
    @Value("${naver.cloud.id-ocr.base-url}") private String baseUrl;

    public IdCardOcrResponse recognizeIdentityCard(MultipartFile file) throws IOException {
        String jsonMessage = IdCardOcrUtil.buildJsonMessage(file);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("message", jsonMessage);
        body.add("file", new MultipartInputStreamFileResource(file));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("X-OCR-SECRET", secretKey);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, requestEntity, String.class);
        return parseResponse(response.getBody());
    }

    private IdCardOcrResponse parseResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode images = root.path("images");

        if (images.isMissingNode() || images.isEmpty()) {
            throw exceptionDbService.getException("OCR_003");
        }

        JsonNode idCard = images.get(0).path("idCard").path("result");
        if (idCard.isMissingNode() || idCard.isNull()) {
            throw exceptionDbService.getException("OCR_004");
        }

        String idType = idCard.path("idtype").asText();
        if ("ID Card".equalsIgnoreCase(idType)) {
            return parseIdCardByType(idCard, IdCardType.ID_CARD);
        } else if ("Driver's License".equalsIgnoreCase(idType)) {
            return parseIdCardByType(idCard, IdCardType.DRIVERS_LISCENSE);
        } else {
            throw exceptionDbService.getException("OCR_002");
        }
    }

    private IdCardOcrResponse parseIdCardByType(JsonNode idCard, IdCardType idCardType) {
        IdCardOcrResponse response = new IdCardOcrResponse();
        String parentField = idCardType.getParentField();

        response.setName(IdCardOcrUtil.cleanText(getFieldText(idCard, "name", parentField)));
        response.setPersonalNum(IdCardOcrUtil.cleanText(getFieldText(idCard, "personalNum", parentField)));
        response.setAddress(IdCardOcrUtil.cleanText(getFieldText(idCard, "address", parentField)));
        response.setIssueDate(IdCardOcrUtil.cleanText(getFieldText(idCard, "issueDate", parentField)));
        response.setAuthority(IdCardOcrUtil.cleanText(getFieldText(idCard, "authority", parentField)));

        return response;
    }

    private String getFieldText(JsonNode node, String fieldName, String parentField) {
        JsonNode fieldNode = node.path(parentField).path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isEmpty()) {
            return "";
        }
        return fieldNode.get(0).path("text").asText("");
    }
}