package dev.crepe.domain.channel.actor.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.service.impl.ActorServiceImpl;
import dev.crepe.domain.channel.actor.service.impl.ActorSubscribeServiceImpl;
import dev.crepe.domain.core.subscribe.model.dto.request.SubscribeProductRequest;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeProductResponse;
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


    @PostMapping("/subscribe")
    @ActorAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<SubscribeProductResponse> subscribeProduct(AppAuthentication auth,
                                                                     @RequestBody SubscribeProductRequest request) {

        SubscribeProductResponse res = actorSubscribeService.subscribeProduct(auth.getUserEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }


}
