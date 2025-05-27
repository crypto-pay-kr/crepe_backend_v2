package dev.crepe.domain.channel.actor.controller;

import dev.crepe.domain.admin.dto.response.GetProductDetailResponse;
import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.channel.actor.service.impl.ActorSubscribeServiceImpl;
import dev.crepe.domain.core.product.model.dto.response.GetOnsaleProductListReponse;
import dev.crepe.domain.core.subscribe.model.dto.request.SubscribeProductRequest;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class ActorSubscribeController {

    private final ActorSubscribeServiceImpl actorSubscribeService;


    @PostMapping("/check-eligibility")
    @ActorAuth
    public ResponseEntity<Boolean> checkEligibility(
            @RequestParam Long productId,
            AppAuthentication auth) {
        boolean isEligible = actorSubscribeService.checkEligibility(productId, auth.getUserEmail());
        return ResponseEntity.ok(isEligible);
    }
    @Operation(
            summary = "상품 구독",
            description = "은행이 발행한 ACTIVE 상품을 구독"
    )
    @PostMapping("/subscribe")
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<SubscribeProductResponse> subscribeProduct(AppAuthentication auth,
                                                                     @RequestBody SubscribeProductRequest request) {

        SubscribeProductResponse res = actorSubscribeService.subscribeProduct(auth.getUserEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @Operation(summary = "은행 등록 상품 목록 조회(Actor 용)",
            description = "일반 Actor가 조회 가능한 등록 상품 목록")
    @GetMapping()
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<List<GetOnsaleProductListReponse>> getAllProducts(AppAuthentication auth) {
        List<GetOnsaleProductListReponse> productList = actorSubscribeService.getAllBankProducts(auth.getUserEmail());
        return ResponseEntity.ok(productList);
    }

    @Operation(summary = "은행 등록 상품 단일 조회(Actor 용)",
            description = "일반 Actor가 특정 상품을 조회")
    @GetMapping("/{productId}")
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<GetProductDetailResponse> getProductById(@PathVariable Long productId, AppAuthentication auth) {
        GetProductDetailResponse product =actorSubscribeService.getProductById(productId, auth.getUserEmail());
        return ResponseEntity.ok(product);
    }


}
