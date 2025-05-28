package dev.crepe.domain.bank.controller;


import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.BankAuth;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.response.GetTokenAccountInfoResponse;
import dev.crepe.domain.bank.model.dto.response.GetTokenHistoryResponse;
import dev.crepe.domain.bank.service.BankTokenManageService;
import dev.crepe.domain.channel.actor.service.ActorExchangeService;
import dev.crepe.domain.channel.actor.service.impl.ActorExchangeServiceImpl;
import dev.crepe.domain.core.util.coin.regulation.model.dto.request.TokenInfoResponse;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.service.TokenPriceService;
import dev.crepe.domain.core.util.history.subscribe.repository.SubscribeHistoryRepository;
import dev.crepe.global.model.dto.GetPaginationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/bank/token")
@RequiredArgsConstructor
public class BankTokenManageController {

    private final BankTokenManageService bankTokenManageService;
    private final TokenPriceService tokenPriceService;
    private final ActorExchangeService actorExchangeService;


    @Operation(summary = "토큰 발행", description = "은행 토큰 발행 요청")
    @PostMapping("/create")
    @BankAuth
    public ResponseEntity<String> createBankToken(@RequestBody @Valid CreateBankTokenRequest request, AppAuthentication auth) {
        bankTokenManageService.createBankToken(request, auth.getUserEmail());
        return ResponseEntity.ok("토큰 발행 요청이 접수되었습니다.");
    }

    @Operation(summary = "토큰 재발행", description = "은행 토큰 재발행 요청")
    @PatchMapping("/recreate")
    @BankAuth
    public ResponseEntity<String> recreateBankToken(@RequestBody @Valid ReCreateBankTokenRequest request, AppAuthentication auth) {
        bankTokenManageService.recreateBankToken(request, auth.getUserEmail());
        return ResponseEntity.ok("토큰 재발행 요청이 접수되었습니다.");
    }



    @Operation(summary = "토큰 계좌 정보 조회", description = "은행 토큰 계좌 정보 조회")
    @GetMapping("/account")
    @BankAuth
    public ResponseEntity<GetTokenAccountInfoResponse> getAccountByBankToken(AppAuthentication auth) {
        GetTokenAccountInfoResponse response = bankTokenManageService.getAccountByBankToken(auth.getUserEmail());
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
        List<GetTokenHistoryResponse> response = bankTokenManageService.getTokenHistory(request);
        return ResponseEntity.ok(response);
    }


    // 토큰 시세 조회
    @Operation(summary = "토큰 시세 조회", description = "해당 은행 토큰의 최신 시세를 조회합니다.")
    @GetMapping("/price")
    @BankAuth
    public ResponseEntity<BigDecimal> getLatestTokenPrice(AppAuthentication auth) {
        BigDecimal latestPrice = bankTokenManageService.getLatestTokenPrice(auth.getUserEmail());
        return ResponseEntity.ok(latestPrice);
    }

    // 토큰 거래량 조회
    @GetMapping("/volume")
    @BankAuth
    @Operation(summary = "토큰 거래량 조회", description = "현재 은행의 토큰 거래량 총합을 조회합니다.")
    public ResponseEntity<BigDecimal> getTokenVolume(AppAuthentication auth) {
        BigDecimal volume = bankTokenManageService.getTotalTokenVolume(auth.getUserEmail());
        return ResponseEntity.ok(volume);
    }


    // 내 토큰 정보 조회
    @GetMapping("/info")
    @BankAuth
    @Operation(summary = "내 은행의 토큰 정보 조회", description = "현재 로그인된 은행의 토큰 정보를 조회합니다.")
    public ResponseEntity<TokenInfoResponse> getMyBankTokenInfo(AppAuthentication auth) {
        BankToken bankToken = bankTokenManageService.getBankTokenByEmail(auth.getUserEmail());
        TokenInfoResponse info = actorExchangeService.getBankTokenInfo(bankToken.getCurrency());
        return ResponseEntity.ok(info);
    }

}

