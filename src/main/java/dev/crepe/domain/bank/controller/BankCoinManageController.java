package dev.crepe.domain.bank.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.BankAuth;
import dev.crepe.domain.bank.model.dto.request.CreateBankAccountRequest;
import dev.crepe.domain.bank.model.dto.response.GetAccountDetailResponse;
import dev.crepe.domain.bank.model.dto.response.GetCoinAccountInfoResponse;
import dev.crepe.domain.bank.service.BankAccountManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
public class BankCoinManageController {

    private final BankAccountManageService bankAccountManageService;

    @Operation(
            summary = "은행 계좌 등록 요청",
            description = "코인 단위(currency), 주소, 태그(optional)를 입력받아 계좌 등록 요청을 보냅니다. 이미 등록된 코인은 중복 등록이 불가합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @BankAuth
    @PostMapping("/register/account")
    public ResponseEntity<Void> registerBankAccount(
            AppAuthentication auth,
            @RequestBody CreateBankAccountRequest request
    ) {
        bankAccountManageService.createBankAccount(request,auth.getUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @Operation(
            summary = "코인 계좌 전체 정보 조회",
            description = "은행이 보유한 코인 계좌의 목록 및 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @BankAuth
    @GetMapping("/account/all")
    public ResponseEntity<List<GetCoinAccountInfoResponse>> getAccountInfoList(AppAuthentication auth) {
        List<GetCoinAccountInfoResponse> accountInfoList = bankAccountManageService.getAccountInfoList(auth.getUserEmail());
        return ResponseEntity.ok(accountInfoList);
    }

    @Operation(
            summary = "코인별 계좌 정보 조회",
            description = "특정 통화(currency)와 이메일(email)을 기반으로 계좌 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @BankAuth
    @GetMapping("/account")
    public ResponseEntity<GetAccountDetailResponse> getAccountByCurrency(
            AppAuthentication auth,
            @RequestParam String currency
    ) {
        GetAccountDetailResponse accountDetail = bankAccountManageService.getAccountByCurrency(currency, auth.getUserEmail());
        return ResponseEntity.ok(accountDetail);
    }

    @Operation(
            summary = "은행 코인 계좌 재등록",
            description = "기존 등록된 코인별 은행 계좌를 새로운 값으로 재등록합니다.",
            security = @SecurityRequirement(name = "bearer-jwt"))
    @PatchMapping("/change/account")
    @BankAuth
    public ResponseEntity<String> changeBankAccount(
            AppAuthentication auth,
            @RequestBody  CreateBankAccountRequest request
    ) {
        bankAccountManageService.changeBankAccount(request,auth.getUserEmail());
        return ResponseEntity.ok(request.getGetAddressRequest().getCurrency() + " 계좌 변경 성공");
    }


    @Operation(
            summary = "은행 코인 계좌 등록 해제",
            description = "기존 등록된 코인별 은행 계좌를 해제 합니다.",
            security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/unregister/account")
    @BankAuth
    public ResponseEntity<String> changeBankAccount(
            AppAuthentication auth,
                @RequestParam String currency
    ) {
        bankAccountManageService.unRegisterBankAccount(currency,auth.getUserEmail());
        return ResponseEntity.ok(currency + " 계좌 해지 성공");
    }



}
