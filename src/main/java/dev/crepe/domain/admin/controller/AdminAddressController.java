package dev.crepe.domain.admin.controller;

import dev.crepe.domain.admin.dto.response.GetPendingWithdrawAddressListResponse;
import dev.crepe.domain.admin.service.AdminAddressService;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.global.auth.role.AdminAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/address")
@AllArgsConstructor
@Tag(name = "Admin Address API", description = "관리자 출금 주소 관리 API")
public class AdminAddressController {

    private final AdminAddressService adminAddressService;

    @Operation(
            summary = "출금 계좌 등록 요청 목록 조회",
            description = "관리자가 승인 대기 중인 출금 계좌 요청 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @AdminAuth
    @GetMapping("/requests")
    public ResponseEntity<Page<GetPendingWithdrawAddressListResponse>> getPendingWithdrawAddressList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "REGISTERING") AddressRegistryStatus status) {
        return ResponseEntity.ok(adminAddressService.getPendingAddressList(page, size, status));
    }


    @Operation(
            summary = "출금 계좌 승인",
            description = "accountId를 기반으로 출금 계좌를 승인 처리합니다. 이미 승인된 계좌는 예외 발생.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @AdminAuth
    @PatchMapping("/approve")
    public ResponseEntity<String> approveStoreAddress(
            @Parameter(description = "승인할 계좌 ID", example = "12")
            @RequestParam("accountId") Long accountId) {
        return ResponseEntity.ok(adminAddressService.approveAddress(accountId));
    }
}