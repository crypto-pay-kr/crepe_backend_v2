package dev.crepe.domain.admin.controller;

import dev.crepe.domain.admin.dto.response.GetPendingBankTokenResponse;
import dev.crepe.domain.admin.service.AdminService;
import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.AdminAuth;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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


    // 토큰 생성 요청 목록 조회

//    @Operation(summary = "토큰 생성 요청 목록 조회", description = "관리자가 토큰 생성 요청 목록을 조회합니다")
//    @AdminAuth
//    @GetMapping("/token")
//    public ResponseEntity<List<GetPendingBankTokenResponse>> getBankTokenRequestList(
//            @RequestParam(value = "page", defaultValue = "0") int page,
//            @RequestParam(value = "size", defaultValue = "10") int size) {
//        List<GetPendingBankTokenResponse> tokenRequests = adminService.getPendingBankTokenResponseList(page, size);
//        return ResponseEntity.ok(tokenRequests);
//    }
//
//

    // 은행 토큰 발행 요청 승인




    // 은행 토큰 발행 요청 반려



}
