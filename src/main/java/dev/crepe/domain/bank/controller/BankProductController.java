package dev.crepe.domain.bank.controller;

import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.BankAuth;
import dev.crepe.domain.bank.model.dto.response.GetAllProductResponse;
import dev.crepe.domain.bank.service.BankProductService;
import dev.crepe.domain.core.product.model.dto.request.RegisterProductRequest;
import dev.crepe.domain.core.product.model.dto.response.RegisterProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
@Tag(name = "Bank Product API", description = "은행 상품 등록 API")
public class BankProductController {
    private final BankProductService bankProductService;


    @Operation(summary = "은행 상품 등록", description = "특정 은행이 상품을 등록하는 api")
    @PostMapping(value = "/register/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @BankAuth
    @SecurityRequirement(name="bearer-jwt")
    public ResponseEntity<RegisterProductResponse> register(AppAuthentication auth, @RequestPart("productImage")
    MultipartFile productImage, MultipartFile guideFile,
                                                            @RequestPart("request") RegisterProductRequest request) {
        RegisterProductResponse res = bankProductService.registerProduct(auth.getUserEmail(),productImage,guideFile,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @Operation(summary = "은행 등록 상품 목록 조회")
    @GetMapping("/products")
    @BankAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<List<GetAllProductResponse>> getProducts(AppAuthentication auth) {
        List<GetAllProductResponse> productList = bankProductService.findAllProductsByBankEmail(auth.getUserEmail());
        return ResponseEntity.ok(productList);
    }


    @Operation(summary = "모든 태그 조회", description = "등록된 모든 태그를 조회합니다.")
    @GetMapping("/products/tags")
    @BankAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<List<String>> getAllTags(AppAuthentication auth) {
        List<String> tags = bankProductService.getAllProductsTags();
        return ResponseEntity.ok(tags);
    }


    @Operation(summary = "은행 등록 상품 단일 조회")
    @GetMapping("/products/{id}")
    @BankAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<GetAllProductResponse> getProductById(AppAuthentication auth, @PathVariable Long id) {
        GetAllProductResponse product = bankProductService.findProductByIdAndBankEmail(auth.getUserEmail(), id);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "판매 중지된 상품 목록 조회")
    @GetMapping("/products/suspended")
    @BankAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<List<GetAllProductResponse>> getSuspendedProducts(AppAuthentication auth) {
        List<GetAllProductResponse> suspendedProducts = bankProductService.findSuspendedProductsByBankEmail(auth.getUserEmail());
        return ResponseEntity.ok(suspendedProducts);
    }





}
