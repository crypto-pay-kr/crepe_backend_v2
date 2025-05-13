package dev.crepe.domain.bank.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.auth.role.BankAuth;
import dev.crepe.domain.bank.model.dto.request.CreateBankAccountRequest;
import dev.crepe.domain.bank.service.BankAccountService;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;


    // TODO : 계좌 생성
    @Operation(
            summary = "출금 계좌 등록 요청",
            description = "코인 단위(currency), 주소, 태그(optional)를 입력받아 계좌 등록 요청을 보냅니다. 이미 등록된 코인은 중복 등록이 불가합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @BankAuth
    @PostMapping("/register/account")
    public ResponseEntity<Void> submitAccountRegistrationRequest(
            AppAuthentication auth,
            @RequestBody CreateBankAccountRequest request
    ) {
        bankAccountService.createBankAccountAddress(request,auth.getUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }



}
