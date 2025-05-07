package dev.crepe.domain.channel.market.like.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.channel.market.like.service.impl.LikeServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
@Tag(name = "User Like API", description = "유저 가맹점 찜기능 관련 api")
public class LikeController {
    private final LikeServiceImpl likeService;

    @PostMapping("/{storeId}")
    @UserAuth
    @Operation(summary = "가게 찜하기", description = "사용자가 특정 가게를 찜합니다.")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<Void> registerStoreLike(@PathVariable Long storeId, AppAuthentication auth) {
        likeService.addLike(auth.getUserEmail(),storeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/{storeId}")
    @UserAuth
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "가게 찜 취소하기", description = "사용자가 특정 가게 찜을 취소합니다.")
    public ResponseEntity<Void> removeStoreLike(@PathVariable Long storeId, AppAuthentication auth) {
        likeService.removeLike(auth.getUserEmail(), storeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
