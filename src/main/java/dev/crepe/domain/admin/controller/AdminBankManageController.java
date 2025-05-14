package dev.crepe.domain.admin.controller;

import dev.crepe.domain.admin.service.AdminProductService;
import dev.crepe.domain.admin.service.impl.AdminProductServiceImpl;
import dev.crepe.domain.core.product.model.dto.request.ReviewProductSubmissionRequest;
import dev.crepe.domain.core.product.model.dto.response.ReviewProductSubmissionResponse;
import dev.crepe.domain.admin.service.AdminService;
import dev.crepe.domain.auth.role.AdminAuth;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/bank")
@RequiredArgsConstructor
@Slf4j
public class AdminBankManageController {

    private final AdminProductServiceImpl adminProductService;
    private final AdminService adminService;

    // 은행 계정 활성화
    @Operation(summary = "은행 계정 활성화", description = "관리자가 특정 은행 계정을 활성화합니다")
    @AdminAuth
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> bankSignup(
            @RequestPart("BankData") @Valid BankSignupDataRequest request,
            @RequestPart("BankCiImage") MultipartFile bankCiImage) {
        adminService.bankSignup(request, bankCiImage);
        return ResponseEntity.ok("은행 계정 활성화 성공");
    }


    // 상품 승인 or 거절
    @Operation(summary = "은행 상품 활성화", description = "관리자가 특정 은행 상품을 활성화,비활성화합니다")
    @AdminAuth
    @PatchMapping(value="/product/review")
    public ResponseEntity<ReviewProductSubmissionResponse> productInspect(
            ReviewProductSubmissionRequest request){
        ReviewProductSubmissionResponse response = adminProductService.reviewProductSubmission(request);
        return ResponseEntity.ok(response);
    }

}
