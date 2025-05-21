package dev.crepe.domain.admin.controller;

import dev.crepe.domain.admin.dto.request.RejectAddressRequest;
import dev.crepe.domain.admin.dto.response.GetPendingWithdrawAddressListResponse;
import dev.crepe.domain.admin.service.AdminAddressService;
import dev.crepe.domain.auth.role.AdminAuth;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/address")
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
            @RequestParam List<AddressRegistryStatus> statuses) {
        return ResponseEntity.ok(adminAddressService.getPendingAddressList(page, size, statuses));
    }


    @Operation(
            summary = "출금 계좌 승인",
            description = "accountId를 기반으로 출금 계좌를 승인 처리합니다. 이미 승인된 계좌는 예외 발생.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @AdminAuth
    @PatchMapping("/approve")
    public ResponseEntity<String> approveAddressRequest(
            @Parameter(description = "승인할 계좌 ID", example = "12")
            @RequestParam("accountId") Long accountId) {
        return ResponseEntity.ok(adminAddressService.approveAddress(accountId));
    }

    @Operation(summary = "출금 계좌 요청 거절", description = "관리자가 은행계좌를 요청을 거절합니다")
    @AdminAuth
    @PatchMapping("/reject/{accountId}")
    public ResponseEntity<String> rejectAddressRequest(@PathVariable Long accountId,
                                                         @RequestBody  RejectAddressRequest reason) {
        adminAddressService.rejectAddress(accountId,reason);
        return ResponseEntity.ok("코인 등록 요청이 거절되었습니다.");
    }

    @Operation(summary = "출금 계좌 해제 요청 승인", description = "관리자가 은행계좌를 해제 요청을 승인 합니다")
    @AdminAuth
    @PatchMapping("/unregister/{accountId}")
    public ResponseEntity<String> unRegisterRequest(@PathVariable Long accountId){
        adminAddressService.unRegisterAddress(accountId);
        return ResponseEntity.ok("코인 등록 요청이 거절되었습니다.");
    }

}