package dev.crepe.domain.admin.controller;


import dev.crepe.domain.admin.dto.response.GetPayHistoryResponse;
import dev.crepe.domain.admin.service.AdminPayHistoryService;
import dev.crepe.domain.admin.service.AdminRefundService;
import dev.crepe.domain.auth.role.AdminAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin pay API", description = "관리자 결제 관련 API")
public class AdminPayController {

    private final AdminRefundService adminRefundService;
    private final AdminPayHistoryService payHistoryAdminService;

    @Operation(
            summary = "환불 요청 승인",
            description = "payId와 사용자 userId로 환불 요청을 승인합니다",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @AdminAuth
    @PatchMapping("/refund")
    public ResponseEntity<String> refundOrder(
            @RequestParam("payId") Long payId,
            @RequestParam("userId") Long id
    ) {
        adminRefundService.approveRefund(payId, id);
        return ResponseEntity.ok("환불 처리가 완료되었습니다.");
    }


    @Operation(
            summary = "유저 결제내역 조회",
            description = "유저 결제 내역 조회",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @AdminAuth
    @GetMapping("/users/{userId}/pay-history")
    public ResponseEntity<Page<GetPayHistoryResponse>> getUserPayHistories(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            Pageable pageable
    ) {
        Page<GetPayHistoryResponse> result = payHistoryAdminService.getPayHistoriesByUserId(userId, type, pageable);
        return ResponseEntity.ok(result);
    }


}
