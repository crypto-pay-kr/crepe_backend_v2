package dev.crepe.domain.admin.controller;


import dev.crepe.domain.admin.dto.response.GetAccountInfoResponse;
import dev.crepe.domain.admin.dto.response.GetActorInfoResponse;
import dev.crepe.domain.admin.dto.response.GetAllTransactionHistoryResponse;
import dev.crepe.domain.admin.service.AdminAccountService;
import dev.crepe.domain.admin.service.AdminActorService;
import dev.crepe.domain.auth.role.AdminAuth;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminActorController {

    private final AdminActorService adminActorService;
    private final AdminAccountService adminAccountService;

    @AdminAuth
    @GetMapping("/admin/actors")
    public ResponseEntity<Page<GetActorInfoResponse>> getActorsByRole(
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminActorService.getActorsByRole(role, page, size));
    }


    @GetMapping("/admin/account")
    @AdminAuth
    public ResponseEntity<Page<GetAccountInfoResponse>> getAccountInfo(
            @RequestParam Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminAccountService.getAccountInfo(id,page,size));
    }


    @GetMapping("/admin/actors/{actorId}/history")
    @AdminAuth
    public ResponseEntity<Page<GetAllTransactionHistoryResponse>> getUserHistory(
            @PathVariable Long actorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<GetAllTransactionHistoryResponse> history = adminAccountService.getUserFullTransactionHistory(actorId, page, size);
        return ResponseEntity.ok(history);
    }







}
