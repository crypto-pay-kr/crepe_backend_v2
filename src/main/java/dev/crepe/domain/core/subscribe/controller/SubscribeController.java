package dev.crepe.domain.core.subscribe.controller;

import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.core.subscribe.expired.service.SubscribeTerminateService;
import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeResponseDto;
import dev.crepe.domain.core.subscribe.model.dto.response.TerminatePreviewDto;
import dev.crepe.domain.core.subscribe.service.SubscribeService;
import dev.crepe.domain.core.util.history.subscribe.model.dto.SubscribeHistoryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscribe")
@RequiredArgsConstructor
@Tag(name = "Subscribe API", description = "상품 가입 API")
public class SubscribeController {

    private final SubscribeService subscribeService;
    private final SubscribeTerminateService subscribeTerminateService;


    @GetMapping("/my")
    @UserAuth
    @Operation(summary = "가입한 상품 조회", description = "가입한 상품 목록을 조회합니다.")
    public ResponseEntity<List<SubscribeResponseDto>> getSubscribes(AppAuthentication auth) {
        return ResponseEntity.ok(subscribeService.getUserSubscribes(auth.getUserEmail()));
    }


    @GetMapping("/history/{subscribeId}")
    @UserAuth
    @Operation(summary = "가입한 상품 조회", description = "가입한 상품 목록을 조회합니다.")
    public ResponseEntity<Slice<SubscribeHistoryDto>> getSubscribeHistorys(@PathVariable Long subscribeId, AppAuthentication auth,
                                                                           @RequestParam int page,
                                                                           @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(subscribeService.getHistoryBySubscribe(subscribeId,auth.getUserEmail(),pageable));
    }


    @GetMapping("/preview/{subscribeId}")
    @UserAuth
    @Operation(summary = "상품 중도 해지시 금액 조회", description = "해당 상품 중도 해지시 금액을 조회합니다.")
    public ResponseEntity<?> terminate(@PathVariable Long subscribeId, AppAuthentication auth) {
        TerminatePreviewDto result = subscribeTerminateService.calculateTerminationPreview(auth.getUserEmail(),subscribeId);
        return ResponseEntity.ok(result);
    }
}
