package dev.crepe.domain.channel.actor.store.controller;

import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.channel.actor.store.model.dto.response.GetOneStoreDetailResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.GetOpenStoreResponse;
import dev.crepe.domain.channel.actor.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
@Tag(name = "Store User API", description = "쇼핑몰 유저 관련 api")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "영업중인 가게 조회", description = "유저가 현재 영업 중인 가게를 조회합니다.")
    @GetMapping
    @UserAuth
    @SecurityRequirement(name="bearer-jwt")
    public ResponseEntity<List<GetOpenStoreResponse>> getOpenStoreList() {
        List<GetOpenStoreResponse> res = storeService.getAllOpenStoreList();
        return new ResponseEntity<>(res,HttpStatus.OK);
    }


    @Operation(summary = "특정 가게의 전체 정보 조회", description = "영업중인 리스트에서 선택하여 특정 가게를 조회하는 경우")
    @GetMapping("/{storeId}")
    @UserAuth
    @SecurityRequirement(name="bearer-jwt")
    public ResponseEntity<GetOneStoreDetailResponse> getOneStoreDetail(@PathVariable Long storeId) {
        GetOneStoreDetailResponse res = storeService.getOneStoreDetail(storeId);
        return new ResponseEntity<>(res,HttpStatus.OK);
    }



}
