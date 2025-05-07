package dev.crepe.domain.channel.actor.store.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.SellerAuth;
import dev.crepe.domain.channel.actor.store.model.dto.request.*;
import dev.crepe.domain.channel.actor.store.model.dto.response.ChangeBusinessInfoResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.ChangeCoinStatusResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.ChangeStoreStatusResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.GetMyStoreAllDetailResponse;
import dev.crepe.domain.channel.actor.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
@Tag(name = "Store Seller API", description = "가맹점 관련 api")
public class StoreSellerController {

    private final StoreService storeService;

    @Operation(summary = "가맹점 회원가입", description = "가맹점 회원으로 회원가입합니다. 가게 이미지와 사업자 등록증 이미지는 자동으로 S3에 업로드됩니다.")
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> storeSignup(
            @RequestPart("storeData") @Valid StoreSignupRequest request,
            @RequestPart("storeImage") MultipartFile storeImage,
            @RequestPart("businessImage") MultipartFile businessImage) {
        storeService.storeSignup(request, storeImage, businessImage);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Operation(summary = "가게 주소 변경", description = "가맹점 주소를 변경합니다.")
    @PatchMapping("/change/address")
    @SellerAuth
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<Void> changeAddress(@Valid @RequestBody ChangeAddressRequest request, AppAuthentication auth) {
        storeService.changeAddress(request, auth.getUserEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "가게 대표이미지 변경", description = "가맹점 대표 이미지를 변경합니다.")
    @PatchMapping(value="/change/image",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SellerAuth
    @SecurityRequirement(name="bearer-jwt")
    public ResponseEntity<String> changeStoreImage(@RequestPart("storeImage") MultipartFile storeImage, AppAuthentication auth) {
        String res = storeService.changeStoreImage(storeImage, auth.getUserEmail());
        return new ResponseEntity<>(res, HttpStatus.OK);

    }

    @Operation(summary = "사업자 등록번호, 등록증 변경", description = "가맹점의 사업자 등록번호와 등록증 정보를 수정합니다.")
    @PatchMapping(value="/change/business",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SellerAuth
    @SecurityRequirement(name="bearer-jwt")
    public ResponseEntity<ChangeBusinessInfoResponse> changeBusinessInfo(@RequestPart("businessNumber") ChangeBusinessInfoRequest request,
                                                                         @RequestPart("businessImage") MultipartFile businessImage,
                                                                         AppAuthentication auth){
        ChangeBusinessInfoResponse res = storeService.changeBusinessInfo(request.getBusinessNumber(), businessImage,auth.getUserEmail());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * (가맹점) 결제 수단 지원 설정
     */
    @Operation(summary = "가맹점 결제 수단 지원 설정", description = "가맹점에서 지원하는 코인 종류를 등록하고 수정합니다.")
    @PatchMapping("/select/coin")
    @SellerAuth
    @SecurityRequirement(name="bearer-jwt")
    public ResponseEntity<ChangeCoinStatusResponse> registerStoreCoin(@RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "지원하는 코인 목록",
            content = @Content(
                    schema = @Schema(implementation = ChangeCoinStatusRequest.class),
                    examples = {
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "코인 지원 설정 예시",
                                    value = "{\"supportedCoins\": [\"XRP\", \"USDT\", \"SOL\"]}",
                                    summary = "코인 지원 설정 예시"
                            ) } )) ChangeCoinStatusRequest request, AppAuthentication auth) {
        ChangeCoinStatusResponse res = storeService.registerStoreCoin(request, auth.getUserEmail());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * 가게 영업중 상태 변경
     */
    @Operation(summary = "가맹점 영업 상태 변경", description = "가맹점 영업 중 상태를 변경합니다.(영업중, 마감)")
    @PatchMapping("/change/status")
    @SellerAuth
    @SecurityRequirement(name="bearer-jwt")
    public ResponseEntity<ChangeStoreStatusResponse> changeStoreStatus(@RequestBody ChangeStoreStatusRequest request, AppAuthentication auth) {
        ChangeStoreStatusResponse res = storeService.changeStoreStatus(request, auth.getUserEmail());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * 내 가게 정보 조회(가맹점)
     */
    @Operation(summary = "내 가게 정보 조회", description = "내 가게 정보와 삭제되지 않은 메뉴를 모두 조회합니다.")
    @GetMapping("/my")
    @SellerAuth
    @SecurityRequirement(name="bearer-jwt")
    public ResponseEntity<GetMyStoreAllDetailResponse> getMyStoreDetail(AppAuthentication auth) {
        GetMyStoreAllDetailResponse res = storeService.getMyStoreAllDetails(auth.getUserEmail());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
