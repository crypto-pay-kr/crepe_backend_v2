package dev.crepe.infra.naver.ocr;

import dev.crepe.infra.naver.ocr.business.dto.BusinessOcrResponse;
import dev.crepe.infra.naver.ocr.business.service.BusinessOcrService;
import dev.crepe.infra.naver.ocr.id.entity.dto.IdCardOcrResponse;
import dev.crepe.infra.naver.ocr.id.service.IdCardOcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
public class OcrController {
    private final BusinessOcrService businessOcrService;
    private final IdCardOcrService idOcrService;

    @PostMapping(value = "/id-card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IdCardOcrResponse> processIdentityCard(@RequestParam("file") MultipartFile file) throws IOException {
        IdCardOcrResponse response = idOcrService.recognizeIdentityCard(file);
        return ResponseEntity.ok((response));
    }

    @PostMapping("/business-license")
    public ResponseEntity<BusinessOcrResponse> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        BusinessOcrResponse result = businessOcrService.processMultipartImage(file);
        return ResponseEntity.ok(result);
    }
}
