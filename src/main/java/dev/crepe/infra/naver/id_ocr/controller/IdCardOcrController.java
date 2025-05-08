package dev.crepe.infra.naver.id_ocr.controller;

import dev.crepe.infra.naver.id_ocr.entity.dto.IdCardOcrResponse;
import dev.crepe.infra.naver.id_ocr.service.IdCardOcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
public class IdCardOcrController {

    private final IdCardOcrService idOcrService;

    @PostMapping(value = "/id-card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IdCardOcrResponse> processIdentityCard(@RequestParam("file")MultipartFile file) throws IOException {
        IdCardOcrResponse response = idOcrService.recognizeIdentityCard(file);
        return ResponseEntity.ok((response));
    }
}
