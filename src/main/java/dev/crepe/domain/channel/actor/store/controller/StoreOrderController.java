package dev.crepe.domain.channel.actor.store.controller;

import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.SellerAuth;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.channel.actor.store.model.dto.request.StoreOrderActionRequest;
import dev.crepe.domain.channel.actor.store.model.dto.response.GetOneStoreDetailResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.GetOpenStoreResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.StoreOrderManageResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.StoreOrderResponse;
import dev.crepe.domain.channel.actor.store.service.StoreOrderService;
import dev.crepe.domain.channel.actor.store.service.StoreService;
import dev.crepe.global.error.exception.ExceptionDbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final ExceptionDbService exceptionDbService;

    @Operation(summary = "영업중인 가게 조회", description = "유저가 현재 영업 중인 가게를 조회합니다.")
    @GetMapping
    @UserAuth
    @SecurityRequirement(name="bearer-jwt")
    public ResponseEntity<List<GetOpenStoreResponse>> getOpenStoreList() {
        List<GetOpenStoreResponse> res = storeService.getAllOpenStoreList();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }


    @Operation(summary = "특정 가게의 전체 정보 조회", description = "영업중인 리스트에서 선택하여 특정 가게를 조회하는 경우")
    @GetMapping("/{storeId}")
    @UserAuth
    @SecurityRequirement(name="bearer-jwt")
    public ResponseEntity<GetOneStoreDetailResponse> getOneStoreDetail(@PathVariable Long storeId) {
        GetOneStoreDetailResponse res = storeService.getOneStoreDetail(storeId);
        return new ResponseEntity<>(res,HttpStatus.OK);
    }

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
                    throw exceptionDbService.getException("STORE_ORDER_001");
                }
                response = storeOrderService.acceptOrder(orderId, storeId, request);
                break;
            case "refuse":
                if (request.getRefusalReason() == null) {
                    throw exceptionDbService.getException("STORE_ORDER_002");
                }
                response = storeOrderService.refuseOrder(orderId, storeId, request);
                break;
            case "complete":
                response = storeOrderService.completeOrder(orderId, storeId);
                break;
            case "cancel":
                response = storeOrderService.cancelOrder(orderId, storeId);
                break;
            default:
                throw exceptionDbService.getException("STORE_ORDER_003");
        }

        return ResponseEntity.ok(response);
    }

}