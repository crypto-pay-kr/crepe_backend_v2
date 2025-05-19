package dev.crepe.domain.core.subscribe.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeResponseDto;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.service.SubscribeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscribe")
@RequiredArgsConstructor
@Tag(name = "Subscribe API", description = "상품 가입 API")
public class SubscribeController {

    private final SubscribeService subscribeService;

    @GetMapping("/my")
    @UserAuth
    @Operation(summary = "가입한 상품 조회", description = "가입한 상품 목록을 조회합니다.")
    public ResponseEntity<List<SubscribeResponseDto>> getSubscribes( AppAuthentication auth) {
        return ResponseEntity.ok(subscribeService.getUserSubscribes(auth.getUserEmail()));
    }
}
