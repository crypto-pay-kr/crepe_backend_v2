package dev.crepe.domain.channel.market.order.controller;

import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.channel.market.order.model.dto.request.CreateOrderRequest;
import dev.crepe.domain.channel.market.order.model.dto.response.CreateOrderResponse;
import dev.crepe.domain.channel.market.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "유저 주문 관련 API")
public class OrderController {

    private final OrderService orderService;

    // 사용자의 주문 목록 조회
    @GetMapping
    @UserAuth
    @Operation(summary = "주문 목록 조회", description = "특정 사용자의 주문 목록을 조회합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<List<CreateOrderResponse>> getCustomerOrderList(AppAuthentication auth) {
        List<CreateOrderResponse> orders = orderService.getCustomerOrderList(auth.getUserEmail());
        return ResponseEntity.ok(orders);
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}")
    @UserAuth
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 내역을 조회합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<CreateOrderResponse> getOrderDetails(@Parameter(description = "주문 ID", example = "ORDER123456") @PathVariable String orderId, AppAuthentication auth) {
        CreateOrderResponse orderResponse = orderService.getOrderDetails(orderId, auth.getUserEmail());
        return ResponseEntity.ok(orderResponse);
    }

    // 주문 생성
    @PostMapping("/create")
    @UserAuth
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
    public  ResponseEntity<Map<String, String>> createOrder(@RequestBody CreateOrderRequest orderRequest, AppAuthentication auth) {
        Map<String, String> response = orderService.createOrder(orderRequest, auth.getUserEmail());
        return ResponseEntity.ok(response);
    }


    // 주문 가능한 결제 수단 조회
    @GetMapping("/available-currency")
    @UserAuth
    @Operation(summary = "주문 가능한 계좌 조회", description = "사용자가 주문 가능한 결제수단을 조회합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<List<String>> getAvailableCurrency(
            @RequestParam Long storeId,
            AppAuthentication auth) {
        List<String> currencies = orderService.getAvailableCurrency(auth.getUserEmail(), storeId);
        return ResponseEntity.ok(currencies);
    }

}
