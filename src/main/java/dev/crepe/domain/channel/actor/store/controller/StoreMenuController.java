package dev.crepe.domain.channel.actor.store.controller;


import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.SellerAuth;
import dev.crepe.domain.channel.actor.store.model.dto.request.RegisterOrChangeMenuRequest;
import dev.crepe.domain.channel.actor.store.model.dto.response.GetMenuDetailResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.GetMenuListResponse;
import dev.crepe.domain.channel.actor.store.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/store/menu")
@RequiredArgsConstructor
@Tag(name = "Store Menu API", description = "가맹점 메뉴 관련 API")
public class StoreMenuController {
    private final MenuService menuService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SellerAuth
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "가맹점 메뉴 등록", description = "가맹점 메뉴를 등록합니다.")
    public ResponseEntity<Void> registerStoreMenu(@RequestPart("menuData") RegisterOrChangeMenuRequest request,
                                                  @Parameter(description = "메뉴 이미지 파일")
                                                  @RequestPart("menuImage") MultipartFile menuImage, AppAuthentication auth) {
        menuService.registerStoreMenu(request, menuImage, auth.getUserEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping(value="/{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SellerAuth
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "가맹점 메뉴 수정", description = "가맹점 메뉴를 수정합니다.")
    public ResponseEntity<Void> changeStoreMenu(
                                                @Parameter(description = "메뉴 ID", example = "1") @PathVariable Long menuId, @RequestPart("menuData") RegisterOrChangeMenuRequest request,
                                                @RequestPart("menuImage") MultipartFile menuImage, AppAuthentication auth) {
        menuService.changeStoreMenu(request, menuId, menuImage, auth.getUserEmail());
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PatchMapping("/{menuId}/status")
    @SellerAuth
    @SecurityRequirement(name="bearer-jwt")
    @Operation(summary = "가맹점 메뉴 삭제", description = "가맹점 메뉴를 삭제합니다.")
    public ResponseEntity<Void> deleteStoreMenu(@Parameter(description = "메뉴 ID", example = "1") @PathVariable Long menuId, AppAuthentication auth) {
        menuService.deleteStoreMenu(menuId, auth.getUserEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 특정 가게 특정 메뉴 1개 조회(가맹점 수정 시)
     */
    @GetMapping("/{menuId}")
    @SellerAuth
    @SecurityRequirement(name="bearer-jwt")
    @Operation(summary = "특정 메뉴에 대한 정보 조회", description = "가맹점이 수정을 위해 메뉴 하나를 눌렀을때 수정화면에서 쓰일 api")
    public ResponseEntity<GetMenuDetailResponse> getMenuDetails(@Parameter(description = "메뉴 ID", example = "1") @PathVariable Long menuId, AppAuthentication auth) {
        GetMenuDetailResponse res = menuService.getMenuDetailById(menuId, auth.getUserEmail());
        return new ResponseEntity<>(res,HttpStatus.OK);
    }


    // 가맹점 전체 메뉴 조회
    @GetMapping("/list/{storeId}")
    @SellerAuth
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "가맹점 전체 메뉴 조회", description = "가맹점이 등록한 모든 메뉴 리스트를 조회합니다.")
    public ResponseEntity<GetMenuListResponse> getAllMenusByStore(
            @Parameter(description = "가맹점 ID", example = "1") @PathVariable Long storeId,
            AppAuthentication auth) {
        GetMenuListResponse response = menuService.getAllMenusByStore(storeId, auth.getUserEmail());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}


