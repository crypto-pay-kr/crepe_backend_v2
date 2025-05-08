package dev.crepe.infra.naver.id_ocr.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class IdCardOcrUtil {


    public static String buildJsonMessage(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String extension = getExtension(fileName);
        return "{\n" +
                "  \"version\": \"V2\",\n" +
                "  \"requestId\": \"" + UUID.randomUUID() + "\",\n" +
                "  \"timestamp\": " + System.currentTimeMillis() + ",\n" +
                "  \"images\": [\n" +
                "    {\n" +
                "      \"name\": \"" + fileName + "\",\n" +
                "      \"format\": \"" + extension + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    public static String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx > 0) ? filename.substring(idx + 1).toLowerCase() : "";
    }

    public static String cleanText(String text) {
        return text.replaceAll("[\\r\\n]+", "")      // 줄바꿈 제거
                .replaceAll("\\s{2,}", " ")       // 연속 공백 → 단일 공백
                .trim();                          // 양끝 공백 제거
    }

}
