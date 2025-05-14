package dev.crepe.domain.admin.controller;


import dev.crepe.domain.admin.service.AdminRefundService;
import dev.crepe.domain.auth.role.AdminAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/refund")
@RequiredArgsConstructor
@Tag(name = "Admin refund API", description = "관리자 환불 승인 API")
public class AdminRefundController {

    private final AdminRefundService adminRefundService;

    @Operation(
            summary = "환불 요청 승인",
            description = "payId와 사용자 이메일로 환불 요청을 승인합니다",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @AdminAuth
    @PatchMapping
    public ResponseEntity<String> refundOrder(
            @RequestParam("payId") Long payId,
            @RequestParam("email") String email
    ) {
        adminRefundService.approveRefund(payId, email);
        return ResponseEntity.ok("환불 처리가 완료되었습니다.");
    }
}
