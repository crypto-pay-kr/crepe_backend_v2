package dev.crepe.domain.channel.actor.store.service;

import dev.crepe.domain.channel.actor.store.model.dto.request.*;
import dev.crepe.domain.channel.actor.store.model.dto.response.*;
import dev.crepe.global.model.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface StoreService {

    ApiResponse<ResponseEntity<Void>> storeSignup(StoreSignupRequest request,
                                                  MultipartFile storeImage,
                                                  MultipartFile businessImage);
    ResponseEntity<Void> changeAddress(ChangeAddressRequest request, String email);
    ResponseEntity<Void> changeName(ChangeStoreNameRequest request, String email);
    String changeStoreImage(MultipartFile storeImage, String email);
    ChangeBusinessInfoResponse changeBusinessInfo(String businessNumber, MultipartFile businessImage, String email);
    ChangeCoinStatusResponse registerStoreCoin(ChangeCoinStatusRequest request, String email);
    ChangeStoreStatusResponse changeStoreStatus(ChangeStoreStatusRequest request, String email);
    GetMyStoreAllDetailResponse getMyStoreAllDetails(String email);

    List<GetOpenStoreResponse> getAllOpenStoreList();

    GetOneStoreDetailResponse getOneStoreDetail(Long storeId);

    Long getStoreIdByEmail(String email);
}
