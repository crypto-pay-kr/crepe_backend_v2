package dev.crepe.domain.channel.actor.controller;

import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.channel.actor.service.impl.ActorSubscribeServiceImpl;
import dev.crepe.domain.core.subscribe.model.dto.request.SubscribeProductRequest;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class ActorSubscribeController {

    private final ActorSubscribeServiceImpl actorSubscribeService;

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


}
