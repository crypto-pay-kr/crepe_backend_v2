package dev.crepe.domain.admin.controller;


import dev.crepe.domain.admin.dto.request.ChangeBankStatusRequest;
import dev.crepe.domain.admin.dto.request.ChangeProductSaleRequest;
import dev.crepe.domain.admin.dto.response.*;
import dev.crepe.domain.admin.service.AdminProductService;
import dev.crepe.domain.bank.model.dto.response.GetCoinAccountInfoResponse;
import dev.crepe.domain.core.product.model.dto.request.ReviewProductSubmissionRequest;
import dev.crepe.domain.core.product.model.dto.response.ReviewProductSubmissionResponse;
import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.admin.service.AdminBankManageService;
import dev.crepe.domain.auth.role.AdminAuth;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    private final AdminProductService adminProductService;
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

    @Operation(summary = "토큰 발행 요청 목록 조회", description = "관리자가 토큰 발행 요청 목록을 조회합니다")
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
    @PatchMapping("/token/approve/{tokenHistoryId}")
    public ResponseEntity<String> approveBankTokenRequest(@PathVariable Long tokenHistoryId) {
        adminBankManageService.approveBankTokenRequest(tokenHistoryId);
        return ResponseEntity.ok("토큰 발행 요청이 승인되었습니다.");
    }


    // 은행 토큰 발행 요청 반려
    @Operation(summary = "은행 토큰 발행 요청 반려", description = "관리자가 특정 은행 토큰 발행 요청을 반려합니다")
    @AdminAuth
    @PatchMapping("/token/reject/{tokenHistoryId}")
    public ResponseEntity<String> rejectBankTokenRequest(@PathVariable Long tokenHistoryId,
                                                         @RequestBody @Valid RejectBankTokenRequest request) {
        adminBankManageService.rejectBankTokenRequest(request, tokenHistoryId);
        return ResponseEntity.ok("토큰 발행 요청이 반려되었습니다.");
    }


    // 상품 승인 or 거절
    @Operation(summary = "은행 상품 활성화", description = "관리자가 특정 은행 상품을 승인, 거절 합니다")
    @AdminAuth
    @PatchMapping(value="/product/review")
    public ResponseEntity<ReviewProductSubmissionResponse> changeProductStatus(
            @RequestBody ReviewProductSubmissionRequest request){
        ReviewProductSubmissionResponse response = adminProductService.reviewProductSubmission(request);
        return ResponseEntity.ok(response);
    }

    // 상품 판매정지(승인 -> 판매정지)
    @Operation(summary = "은행 상품 판매정지, 해제", description = "관리자가 특정 은행 상품을 판매정지,해제 합니다")
    @AdminAuth
    @PatchMapping(value="/product/suspend")
    public ResponseEntity<ReviewProductSubmissionResponse> changeProductSalesStatus(
            @RequestBody ChangeProductSaleRequest request){
        ReviewProductSubmissionResponse response = adminProductService.changeProductSalesStatus(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary="상품 발행 요청 조회",description = "관리자가 특정 은행의 발행 상품을 조회")
    @AdminAuth
    @GetMapping("/product/{bankId}")
    public ResponseEntity<List<GetAllProductResponse>> getAllProductList(@PathVariable Long bankId) {
        List<GetAllProductResponse> response = adminBankManageService.getAllBankProducts(bankId);
        return ResponseEntity.ok(response);
    }



    @Operation(summary="판매정지 상품 요청 조회",description = "관리자가 특정은행의 판매정지 상품 조회")
    @AdminAuth
    @GetMapping("/product/{bankId}/suspend")
    public ResponseEntity<List<GetAllProductResponse>> getSuspendedProductList(@PathVariable Long bankId){
        List<GetAllProductResponse> response = adminBankManageService.getSuspendedBankProducts(bankId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary="모든 은행 조회",description = "관리자가 가입된 모든 은행 조회")
    @AdminAuth
    @GetMapping
    public ResponseEntity<List<GetAllBankResponse>> getAllBankList(){
        List<GetAllBankResponse> res = adminBankManageService.getAllActiveBankInfoList();
        return ResponseEntity.ok(res);
    }

    @Operation(summary="모든 이용정지 은행 조회",description = "관리자가 가입된 이용정지 은행 조회")
    @AdminAuth
    @GetMapping("/suspend")
    public ResponseEntity<List<GetAllSuspendedBankResponse>> getAllSuspendedBankList(){
        List<GetAllSuspendedBankResponse> res = adminBankManageService.getAllSuspendedBankInfoList();
        return ResponseEntity.ok(res);
    }

    @Operation(summary="특정 은행 계정 정지, 재활성화",description = "관리자가 특정 은행 계정 정지, 재 활성화")
    @AdminAuth
    @PatchMapping("/status")
    public ResponseEntity<String> changeBankStatus(@RequestBody ChangeBankStatusRequest request){
        adminBankManageService.changeBankStatus(request);
        return ResponseEntity.ok("은행 상태 변경 성공");
    }

    //TODO : 관리자가 특정 은행 계좌 조회
    @Operation(
            summary = "특정 은행 계좌 정보 조회",
            description = "bankId를 기반으로 특정 은행 계좌를 조회",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @AdminAuth
    @GetMapping("/account/{bankId}")
    public ResponseEntity<List<GetCoinAccountInfoResponse>> getBankInfoDetailByAdmin(@PathVariable Long bankId) {
        List<GetCoinAccountInfoResponse> res = adminBankManageService.getBankAccountByAdmin(bankId);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @Operation(summary = "은행 출금 계좌 정지", description = "관리자가 특정 은행 계좌를 정지시킵니다")
    @AdminAuth
    @PatchMapping("/hold/{accountId}")
    public ResponseEntity<String> holdAddressRequest(@PathVariable Long accountId){
        adminBankManageService.holdBankAddress(accountId);
        return ResponseEntity.ok("계좌가 정지되었습니다.");
    }

    @Operation(summary="상품 상세 조회 api",description = "관리자가 특정 상품 정보를 상세조회")
    @AdminAuth
    @GetMapping("/{bankId}/product/{productId}")
    public ResponseEntity<GetProductDetailResponse> getProductDetail(@PathVariable Long bankId, @PathVariable Long productId) {
        GetProductDetailResponse response = adminBankManageService.getBankProductDetail(bankId,productId);
        return ResponseEntity.ok(response);
    }

    
}
