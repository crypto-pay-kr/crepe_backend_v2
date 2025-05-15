package dev.crepe.domain.admin.controller;


import dev.crepe.domain.admin.dto.response.GetAllBankTokenResponse;
import dev.crepe.domain.admin.service.AdminBankManageService;
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

import java.util.List;


@RestController
@RequestMapping("/admin/bank")
@RequiredArgsConstructor
@Slf4j
public class AdminBankManageController {

    private final AdminBankManageService adminBankManageService;

    // 은행 계정 활성화
    @Operation(summary = "은행 계정 활성화", description = "관리자가 특정 은행 계정을 활성화합니다")
    @AdminAuth
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> bankSignup(
            @RequestPart("BankData") @Valid BankSignupDataRequest request,
            @RequestPart("BankCiImage") MultipartFile bankCiImage) {
        adminBankManageService.bankSignup(request, bankCiImage);
        return ResponseEntity.ok("은행 계정 활성화 성공");
    }


    // 토큰 생성 요청 목록 조회
    @Operation(summary = "토큰 생성 요청 목록 조회", description = "관리자가 토큰 생성 요청 목록을 조회합니다")
    @AdminAuth
    @GetMapping("/token")
    public ResponseEntity<List<GetAllBankTokenResponse>> getBankTokenRequestList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        List<GetAllBankTokenResponse> tokenRequests = adminBankManageService.getAllBankTokenResponseList(page, size);
        return ResponseEntity.ok(tokenRequests);
    }



    // 은행 토큰 발행 요청 승인
    @Operation(summary = "은행 토큰 발행 요청 승인", description = "관리자가 특정 은행 토큰 발행 요청을 승인합니다")
    @AdminAuth
    @PatchMapping("/token/approve/{tokenId}")
    public ResponseEntity<String> approveBankTokenRequest(@PathVariable Long tokenId) {
        adminBankManageService.approveBankTokenRequest(tokenId);
        return ResponseEntity.ok("토큰 발행 요청이 승인되었습니다.");
    }




    // 은행 토큰 발행 요청 반려
    @Operation(summary = "은행 토큰 발행 요청 반려", description = "관리자가 특정 은행 토큰 발행 요청을 반려합니다")
    @AdminAuth
    @PatchMapping("/token/reject/{tokenId}")
    public ResponseEntity<String> refuseBankTokenRequest(@PathVariable Long tokenId) {
        adminBankManageService.rejectBankTokenRequest(tokenId);
        return ResponseEntity.ok("토큰 발행 요청이 반려되었습니다.");
    }



}
