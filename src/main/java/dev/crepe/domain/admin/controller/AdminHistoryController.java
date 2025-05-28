package dev.crepe.domain.admin.controller;


import dev.crepe.domain.admin.dto.response.GetPayHistoryResponse;
import dev.crepe.domain.admin.dto.response.GetSettlementHistoryResponse;
import dev.crepe.domain.admin.service.AdminHistoryService;
import dev.crepe.domain.admin.service.AdminRefundService;
import dev.crepe.domain.auth.role.AdminAuth;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin pay API", description = "관리자 결제 관련 API")
public class AdminHistoryController {

    private final AdminRefundService adminRefundService;
    private final AdminHistoryService adminHistoryService;

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
        Page<GetPayHistoryResponse> result = adminHistoryService.getPayHistoriesByUserId(userId, type, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "가맹점 정산내역 조회",
            description = "가맹점 정산  내역 조회",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @AdminAuth
    @GetMapping("/store/{storeId}/settlement-history")
    public ResponseEntity<Page<GetSettlementHistoryResponse>> getUserPayHistories(
            @PathVariable Long storeId,
            @RequestParam(required = false) TransactionStatus status,
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC)Pageable pageable
    ) {
        Page<GetSettlementHistoryResponse> result = adminHistoryService.getSettlementHistoriesByUserId(storeId, status, pageable);
        return ResponseEntity.ok(result);
    }


    @Operation(
            summary = "가맹점 정산 재요청",
            description = "가맹점 정산 재요청",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @AdminAuth
    @GetMapping("/store/{historyId}/re-settlement")
    public ResponseEntity<String> getUserPayHistories(
            @PathVariable Long historyId
    ) {
        adminHistoryService.reSettlement(historyId);
        return ResponseEntity.ok("정산 완료");
    }




}
