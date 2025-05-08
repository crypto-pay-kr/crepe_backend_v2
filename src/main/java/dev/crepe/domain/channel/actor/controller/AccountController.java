package dev.crepe.domain.channel.actor.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import dev.crepe.domain.core.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Account API", description = "계좌 관련 API")
public class AccountController {

    private final AccountService accountService;


    @Operation(
            summary = "출금 계좌 등록 요청",
            description = "코인 단위(currency), 주소, 태그(optional)를 입력받아 계좌 등록 요청을 보냅니다. 이미 등록된 코인은 중복 등록이 불가합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @PostMapping("/save/address")
    public ResponseEntity<Void> submitAccountRegistrationRequest(
            AppAuthentication auth,
            @RequestBody GetAddressRequest request
    ) {
        accountService.submitAccountRegistrationRequest(request, auth.getUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "출금 계좌 주소 조회",
            description = "코인 단위(currency) 기준으로 등록된 출금 주소를 조회합니다. 등록 중일 경우 '진행중' 상태 메시지를 반환합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @GetMapping("/address")
    public ResponseEntity<GetAddressResponse> getAddress(
            @Parameter(description = "코인 단위 (예: XRP, SOL, USDT)", example = "XRP")
            @RequestParam("currency") String currency,
            AppAuthentication auth
    ) {
        GetAddressResponse response = accountService.getAddressByCurrency(currency, auth.getUserEmail());
        return ResponseEntity.ok( response);
    }

        @Operation(
            summary = "입금주소 재등록",
            description = "기존 등록된 입금 주소를 새로운 값으로 재등록합니다.",
            security = @SecurityRequirement(name = "bearer-jwt"))
    @PatchMapping("/resave/address")
    @ActorAuth
    public ResponseEntity<Void> reRegisterAddress(
            AppAuthentication auth,
            @RequestBody GetAddressRequest request
    ) {
        accountService.reRegisterAddress(auth.getUserEmail(), request);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "잔액 조회",
            description = "현재 로그인한 유저, 가맹점의 전체 잔액 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @GetMapping("/balance")
    public ResponseEntity<List<GetBalanceResponse>> getBalanceList(AppAuthentication auth) {
        List<GetBalanceResponse> balanceList = accountService.getBalanceList(auth.getUserEmail());
        return ResponseEntity.ok(balanceList);
    }


    @Operation(
            summary = "특정 종목 잔액 조회",
            description = "현재 로그인한 유저, 가맹점의 특정 코인 잔액을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @GetMapping("/balance/{currency}")
    public ResponseEntity<GetBalanceResponse> getBalanceByCurrency(@PathVariable String currency,
                                                                       AppAuthentication auth) {
        GetBalanceResponse response = accountService.getBalanceByCurrency(auth.getUserEmail(), currency);
        return ResponseEntity.ok(response);
    }


}
