package dev.crepe.domain.channel.actor.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.service.impl.ActorServiceImpl;
import dev.crepe.domain.channel.actor.service.impl.ActorSubscribeServiceImpl;
import dev.crepe.domain.core.subscribe.model.dto.request.SubscribeProductRequest;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeProductResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@AllArgsConstructor
public class ActorSubscribeController {

    private final ActorSubscribeServiceImpl actorSubscribeService;

    @UserAuth
    @PostMapping("/subscribe")
    public ResponseEntity<SubscribeProductResponse> subscribeProduct(AppAuthentication auth,
                                                                     @RequestBody SubscribeProductRequest request) {

        SubscribeProductResponse res = actorSubscribeService.subscribeProduct(auth.getUserEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
