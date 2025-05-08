package dev.crepe.infra.naver.ocr;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/ocr")
@AllArgsConstructor
public class BusinessOcrController {

    private final BusinessOcrService ocrService;

    @PostMapping("/business-license")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String result = ocrService.processMultipartImage(file);
        return ResponseEntity.ok(result);
    }
}
