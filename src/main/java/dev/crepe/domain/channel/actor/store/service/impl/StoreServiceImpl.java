package dev.crepe.domain.channel.actor.store.service.impl;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.channel.actor.exception.AlreadyEmailException;
import dev.crepe.domain.channel.actor.exception.AlreadyNicknameException;
import dev.crepe.domain.channel.actor.exception.AlreadyPhoneNumberException;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.store.exception.StoreNotFoundException;
import dev.crepe.domain.channel.actor.store.model.StoreStatus;
import dev.crepe.domain.channel.actor.store.model.dto.request.*;
import dev.crepe.domain.channel.actor.store.model.dto.response.*;
import dev.crepe.domain.channel.actor.store.repository.MenuRepository;
import dev.crepe.domain.channel.actor.store.repository.StoreRepository;
import dev.crepe.domain.channel.actor.store.service.StoreService;
import dev.crepe.domain.channel.market.like.repository.LikeRepository;
import dev.crepe.domain.channel.market.menu.model.entity.Menu;
import dev.crepe.global.model.dto.ApiResponse;
import dev.crepe.infra.s3.service.S3Service;
import dev.crepe.infra.sms.model.InMemorySmsAuthService;
import dev.crepe.infra.sms.model.SmsType;
import dev.crepe.infra.sms.service.SmsManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {
    private final ActorRepository actorRepository;
    private final StoreRepository storeRepository;
    private final LikeRepository likeRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder encoder;
    private final S3Service s3Service;

    private final SmsManageService smsManageService;


    @Transactional
    @Override
    public ApiResponse<ResponseEntity<Void>> storeSignup(
            StoreSignupRequest request,
            MultipartFile storeImage,
            MultipartFile businessImage) {

//        InMemorySmsAuthService.SmsAuthData smsAuthData = smsManageService.getSmsAuthData(request.getPhoneNumber(), SmsType.SIGN_UP);
//        String validatePhone = smsAuthData.getPhoneNumber();

        checkAlreadyField(request);

        String storeImageUrl = s3Service.uploadFile(storeImage, "store-images");
        String businessImageUrl = s3Service.uploadFile(businessImage, "business-licenses");

        Actor store = Actor.builder()
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .phoneNum(request.getPhoneNumber())
                .role(UserRole.SELLER)
                .name(request.getStoreName())
                .storeType(request.getStoreType())
                .storeAddress(request.getStoreAddress())
                .businessNumber(request.getBusinessNumber())
                .businessImage(businessImageUrl)
                .storeImage(storeImageUrl)
                .build();

        actorRepository.save(store);

        return ApiResponse.success("가맹점 회원가입 성공", null);
    }
    private void checkAlreadyField(StoreSignupRequest request) {
        if (actorRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyEmailException();
        }

        if (actorRepository.existsByName(request.getStoreName())) {
            throw new AlreadyNicknameException();
        }

        if (actorRepository.existsByPhoneNum(request.getPhoneNumber())) {
            throw new AlreadyPhoneNumberException();
        }
    }

    @Override
    public ResponseEntity<Void> changeAddress(ChangeAddressRequest request, String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new StoreNotFoundException(userEmail));

        store.changeStoreAddress(request.getNewAddress());
        actorRepository.save(store);
        return ResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public String changeStoreImage(MultipartFile storeImage, String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new StoreNotFoundException(userEmail));

        String oldImageUrl = store.getStoreImage();
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            try {
                String oldKey = s3Service.extractKeyFromUrl(oldImageUrl);
                log.info("Previous store image key: {}", oldKey);
            } catch (Exception e) {
                log.warn("Failed to process old image: {}", oldImageUrl, e);
            }
        }
        String newImageUrl = s3Service.uploadFile(storeImage, "store-images");
        store.changeStoreImage(newImageUrl);
        actorRepository.save(store);
        return newImageUrl;
    }

    @Override
    @Transactional
    public ChangeBusinessInfoResponse changeBusinessInfo(String businessNumber, MultipartFile businessImage, String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new StoreNotFoundException(userEmail));

        String oldImageUrl = store.getBusinessImage();
        if(oldImageUrl != null && !oldImageUrl.isEmpty()) {
            try{
                String oldKey = s3Service.extractKeyFromUrl(oldImageUrl);
                log.info("Previous business image key: {}", oldKey);
            }
            catch (Exception e){
                log.warn("Failed to process old business image: {}", oldImageUrl, e);
            }
        }
        String newImageUrl = s3Service.uploadFile(businessImage, "business-licenses");
        store.changeBusiness(businessNumber,newImageUrl);
        ChangeBusinessInfoResponse res = ChangeBusinessInfoResponse.builder()
                .businessNumber(businessNumber)
                .businessImg(newImageUrl)
                .build();
        return res;
    }

    @Transactional
    @Override
    public ChangeCoinStatusResponse registerStoreCoin(ChangeCoinStatusRequest request, String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new StoreNotFoundException(userEmail));

        store.changeSupportedCoins(request.getSupportedCoins());
        ChangeCoinStatusResponse res = ChangeCoinStatusResponse.builder()
                .supportedCoins(request.getSupportedCoins())
                .build();
        return res;
    }

    @Transactional
    @Override
    public ChangeStoreStatusResponse changeStoreStatus(ChangeStoreStatusRequest request, String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new StoreNotFoundException(userEmail));

        store.changeStoreStatus(request.getStoreStatus());
        ChangeStoreStatusResponse res = ChangeStoreStatusResponse.builder()
                .storeStatus(store.getStatus())
                .build();
        return res;
    }

    @Override
    public GetMyStoreAllDetailResponse getMyStoreAllDetails(String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new StoreNotFoundException(userEmail));

        List<Menu> menus = menuRepository.findAllByStoreId(store.getId());
        List<GetMenuDetailResponse> menuResponse = menus.stream()
                .map(menu -> GetMenuDetailResponse.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getName())
                        .menuPrice(menu.getPrice())
                        .menuImage(menu.getImage())
                        .build())
                .collect(Collectors.toList());

        GetMyStoreAllDetailResponse res = GetMyStoreAllDetailResponse.builder().likeCount(likeRepository.countByStoreAndActiveTrue(store)).storeName(store.getName())
                .storeAddress(store.getStoreAddress()).storeStatus(store.getStatus()).storeImageUrl(store.getStoreImage())
                .coinList(store.getCoinList()).menuList(menuResponse).build();

        return res;
    }

    @Override
    public List<GetOpenStoreResponse> getAllOpenStoreList() {
        List<Actor> actor  = storeRepository.findByDataStatusTrueAndStatus(StoreStatus.OPEN);

        return actor.stream()
                .map(store -> GetOpenStoreResponse.builder()
                        .storeId(store.getId())
                        .storeName(store.getName())
                        .storeImage(store.getStoreImage())
                        .likeCount(likeRepository.countByStoreAndActiveTrue(store))
                        .storeType(store.getStoreType())
                        .coinList(store.getCoinList())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GetOneStoreDetailResponse getOneStoreDetail(Long storeId) {

        Actor store = actorRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가게입니다."));

        Long likeCount = likeRepository.countByStoreAndActiveTrue(store);

        List<Menu> menus = menuRepository.findAllByStoreId(store.getId());
        List<GetMenuDetailResponse> menuResponse = menus.stream()
                .map(menu -> GetMenuDetailResponse.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getName())
                        .menuPrice(menu.getPrice())
                        .menuImage(menu.getImage())
                        .build())
                .collect(Collectors.toList());

        return GetOneStoreDetailResponse.builder()
                .likeCount(likeCount)
                .storeName(store.getName())
                .storeAddress(store.getStoreAddress())
                .storeImageUrl(store.getStoreImage())
                .coinList(store.getCoinList())
                .menuList(menuResponse)
                .build();
    }

    @Override
    public Long getStoreIdByEmail(String email) {
        return actorRepository.findByEmail(email)
                .filter(BaseEntity -> BaseEntity instanceof Actor)
                .map(BaseEntity -> ((Actor) BaseEntity).getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가맹점을 찾을 수 없습니다."));
    }


}