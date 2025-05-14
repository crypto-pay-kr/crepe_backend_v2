package dev.crepe.domain.bank.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.core.product.model.dto.request.RegisterProductRequest;
import dev.crepe.domain.core.product.model.dto.response.RegisterProductResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Bank Product API", description = "은행 상품 등록 API")
public class BankProductController {
    private final BankService bankService;

    @PostMapping("/register/product")
    public ResponseEntity<RegisterProductResponse> register(AppAuthentication auth, @RequestPart("productImage") MultipartFile productImage, @RequestBody RegisterProductRequest request) {
        RegisterProductResponse res = bankService.registerProduct(auth.getUserEmail(),productImage,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

}
