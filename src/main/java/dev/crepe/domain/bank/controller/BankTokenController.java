package dev.crepe.domain.bank.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.BankAuth;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.service.BankTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank/token")
@RequiredArgsConstructor
public class BankTokenController {

    private final BankTokenService bankTokenService;

    // TODO : 은행 토큰 발행 요청 API
    @PostMapping("/create")
    @BankAuth
    public ResponseEntity<String> createBankToken(@RequestBody @Valid CreateBankTokenRequest request, AppAuthentication auth) {
        bankTokenService.createBankToken(request, auth.getUserEmail());
        return ResponseEntity.ok("토큰 발행 요청이 접수되었습니다.");
    }






    // TODO : 토큰 변경 이력 목록 조회 API


    // TODO : 토큰 포토폴리오 조회 API


    // TODO : 토큰 포토폴리오 변경 및 재발행 요청 API


}
