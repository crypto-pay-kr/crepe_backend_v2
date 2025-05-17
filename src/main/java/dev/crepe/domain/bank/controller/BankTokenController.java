package dev.crepe.domain.bank.controller;


import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.BankAuth;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.response.GetTokenAccountInfoResponse;
import dev.crepe.domain.bank.model.dto.response.GetTokenHistoryResponse;
import dev.crepe.domain.bank.service.BankTokenService;
import dev.crepe.global.model.dto.GetPaginationRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bank/token")
@RequiredArgsConstructor
public class BankTokenController {

    private final BankTokenService bankTokenService;


    // 은행 토큰 발행 요청
    @PostMapping("/create")
    @BankAuth
    public ResponseEntity<String> createBankToken(@RequestBody @Valid CreateBankTokenRequest request, AppAuthentication auth) {
        bankTokenService.createBankToken(request, auth.getUserEmail());
        return ResponseEntity.ok("토큰 발행 요청이 접수되었습니다.");
    }

    // 은행 토큰 재발행 요청
    @PatchMapping("/recreate")
    @BankAuth
    public ResponseEntity<String> recreateBankToken(@RequestBody @Valid ReCreateBankTokenRequest request, AppAuthentication auth) {
        bankTokenService.recreateBankToken(request, auth.getUserEmail());
        return ResponseEntity.ok("토큰 재발행 요청이 접수되었습니다.");
    }


    // 토큰 계좌 정보 조회
    @GetMapping("/account")
    @BankAuth
    public ResponseEntity<GetTokenAccountInfoResponse> getAccountByBankToken(AppAuthentication auth) {
        GetTokenAccountInfoResponse response = bankTokenService.getAccountByBankToken(auth.getUserEmail());
        return ResponseEntity.ok(response);
    }



    // 토큰 발행 요청 이력 조회
    @Operation(summary = "토큰 발행 요청 이력 조회", description = "은행의 토큰 발행 요청 이력을 조회합니다.")
    @BankAuth
    @GetMapping("/history")
    public ResponseEntity<List<GetTokenHistoryResponse>> getTokenHistoryByBank(
            AppAuthentication auth,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        GetPaginationRequest request = new GetPaginationRequest(auth.getUserEmail(), page, size);
        List<GetTokenHistoryResponse> response = bankTokenService.getTokenHistory(request);
        return ResponseEntity.ok(response);
    }






}
