package dev.crepe.domain.channel.actor.store.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.SellerAuth;
import dev.crepe.domain.channel.actor.store.exception.InvalidActionException;
import dev.crepe.domain.channel.actor.store.model.dto.request.StoreOrderActionRequest;
import dev.crepe.domain.channel.actor.store.model.dto.response.StoreOrderManageResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.StoreOrderResponse;
import dev.crepe.domain.channel.actor.store.service.StoreOrderService;
import dev.crepe.domain.channel.actor.store.service.StoreService;
import dev.crepe.global.error.exception.LocalizedMessageException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Store Order Management", description = "가맹점 주문 관리 API")
@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreOrderController {

    private final StoreOrderService storeOrderService;
    private final StoreService storeService;



    // 전체 주문 조회
    // TODO: 슬라이싱 필요
    @Operation(summary = "전체 주문 조회", description = "가맹점의 모든 주문을 조회합니다.")
    @SellerAuth
    @GetMapping("/orders")
    public ResponseEntity<List<StoreOrderResponse>> getAllList(AppAuthentication auth) {
        Long storeId = storeService.getStoreIdByEmail(auth.getUserEmail());
        List<StoreOrderResponse> allOrders = storeOrderService.getAllList(storeId);
        return ResponseEntity.ok(allOrders);
    }

    // 가맹점별 접수된 주문 목록 조회
    // TODO: 슬라이싱 필요
    @Operation(summary = "접수된 주문 조회", description = "가맹점별 접수된 주문 목록을 조회합니다.")
    @SellerAuth
    @GetMapping("/orders/waiting")
    public ResponseEntity<List<StoreOrderResponse>> getWaitingList(AppAuthentication auth) {
        Long storeId = storeService.getStoreIdByEmail(auth.getUserEmail());
        List<StoreOrderResponse> waitingOrders = storeOrderService.getWaitingList(storeId);
        return ResponseEntity.ok(waitingOrders);
    }


    // 가맹점별 처리중인 주문 목록 조회
    // TODO: 슬라이싱 필요
    @Operation(summary = "처리 중인 주문 조회", description = "가맹점별 처리 중인 주문 목록을 조회합니다.")
    @SellerAuth
    @GetMapping("/orders/processing")
    public ResponseEntity<List<StoreOrderResponse>> getProcessingList(AppAuthentication auth) {
        Long storeId = storeService.getStoreIdByEmail(auth.getUserEmail());
        List<StoreOrderResponse> processingOrders = storeOrderService.getProcessingList(storeId);
        return ResponseEntity.ok(processingOrders);
    }


    // 주문 관리 (수락, 거절, 완료)
    @Operation(summary = "주문 상태 업데이트", description = "주문을 수락, 거절 또는 완료 상태로 업데이트합니다.")
    @SellerAuth
    @PostMapping("/orders/{orderId}/action")
    public ResponseEntity<StoreOrderManageResponse> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody StoreOrderActionRequest request,
            AppAuthentication auth) {
        Long storeId = storeService.getStoreIdByEmail(auth.getUserEmail());
        StoreOrderManageResponse response;

        switch (request.getAction().toLowerCase()) {
            case "accept":
                if (request.getPreparationTime() == null) {
                    throw new LocalizedMessageException(
                            org.springframework.http.HttpStatus.BAD_REQUEST,
                            "error.preparation.time.required"
                    );
                }
                response = storeOrderService.acceptOrder(orderId, storeId, request);
                break;
            case "refuse":
                if (request.getRefusalReason() == null) {
                    throw new LocalizedMessageException(
                            org.springframework.http.HttpStatus.BAD_REQUEST,
                            "error.refusal.reason.required"
                    );
                }
                response = storeOrderService.refuseOrder(orderId, storeId, request);
                break;
            case "complete":
                response = storeOrderService.completeOrder(orderId, storeId);
                break;
            default:
                throw new InvalidActionException(request.getAction());
        }

        return ResponseEntity.ok(response);
    }
}