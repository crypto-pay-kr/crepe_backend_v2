package dev.crepe.domain.bank.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.BankAuth;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.response.GetTokenAccountInfoResponse;
import dev.crepe.domain.bank.service.BankTokenService;
import dev.crepe.domain.core.account.model.entity.Account;
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
        bankTokenService.reCreateBankToken(request, auth.getUserEmail());
        return ResponseEntity.ok("토큰 재발행 요청이 접수되었습니다.");
    }


    // BankToken 계좌 조회 API
    @GetMapping("/account")
    @BankAuth
    public ResponseEntity<GetTokenAccountInfoResponse> getAccountByBankToken(AppAuthentication auth) {
        GetTokenAccountInfoResponse response = bankTokenService.getAccountByBankToken(auth.getUserEmail());
        return ResponseEntity.ok(response);
    }



    // TODO : 토큰 변경 이력 목록 조회 API


    // TODO : 토큰 포토폴리오 조회 API





}
