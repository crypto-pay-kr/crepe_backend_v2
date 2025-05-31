package dev.crepe.domain.channel.actor.store.service.impl;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.store.exception.StoreNotFoundException;
import dev.crepe.domain.channel.actor.store.exception.UnauthorizedStoreAccessException;
import dev.crepe.domain.channel.actor.store.model.StoreStatus;
import dev.crepe.domain.channel.actor.store.model.dto.request.*;
import dev.crepe.domain.channel.actor.store.model.dto.response.*;
import dev.crepe.domain.channel.actor.store.repository.MenuRepository;
import dev.crepe.domain.channel.actor.store.repository.StoreRepository;
import dev.crepe.domain.channel.actor.store.service.StoreService;
import dev.crepe.domain.channel.market.like.model.entity.Like;
import dev.crepe.domain.channel.market.like.repository.LikeRepository;
import dev.crepe.domain.channel.market.menu.model.entity.Menu;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.model.dto.ApiResponse;
import dev.crepe.infra.s3.service.S3Service;
import dev.crepe.infra.sms.service.SmsManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {
    private final ActorRepository actorRepository;
    private final StoreRepository storeRepository;
    private final CoinRepository coinRepository;
    private final LikeRepository likeRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder encoder;
    private final S3Service s3Service;
    private final ExceptionDbService exceptionDbService;


    // 가맹점 회원가입
    @Transactional
    @Override
    public ApiResponse<ResponseEntity<Void>> storeSignup(
            StoreSignupRequest request,
            MultipartFile storeImage,
            MultipartFile businessImage) {
        log.info("가맹점 회원가입 요청 - 이메일: {}", request.getEmail());

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


    // 입력 필드 체크
    private void checkAlreadyField(StoreSignupRequest request) {
        if (actorRepository.existsByEmail(request.getEmail())) {
            throw exceptionDbService.getException("ACTOR_003"); // 이미 존재하는 이메일
        }

        if (actorRepository.existsByNickName(request.getStoreName())) {
            throw exceptionDbService.getException("ACTOR_004"); // 이미 존재하는 닉네임
        }

        if (actorRepository.existsByPhoneNum(request.getPhoneNumber())) {
            throw exceptionDbService.getException("ACTOR_009"); // 이미 존재하는 휴대폰 번호
        }
    }

    // 가맹점 이름 변경
    @Override
    @Transactional
    public ResponseEntity<Void> changeName(ChangeStoreNameRequest request, String userEmail) {
        log.info("가맹점 이름 변경 시작 - 사용자 이메일: {}", userEmail);
        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        store.changeName(request.getNewStoreName());
        actorRepository.save(store);
        return ResponseEntity.ok(null);
    }


    // 가맹점 주소 변경
    @Override
    @Transactional
    public ResponseEntity<Void> changeAddress(ChangeAddressRequest request, String userEmail) {
        log.info("가맹점 주소 변경 시작 - 사용자 이메일: {}", userEmail);

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        store.changeStoreAddress(request.getNewAddress());
        actorRepository.save(store);
        return ResponseEntity.ok(null);
    }


    // 가맹점 대표 이미지 변경
    @Override
    @Transactional
    public String changeStoreImage(MultipartFile storeImage, String userEmail) {
        log.info("가맹점 대표 이미지 변경 시작 - 사용자 이메일: {}", userEmail);

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        String oldImageUrl = store.getStoreImage();
        if(oldImageUrl != null && !oldImageUrl.isEmpty()) {
            try{
                String oldKey = s3Service.extractKeyFromUrl(oldImageUrl);
            }
            catch (Exception e){
                throw exceptionDbService.getException("S3_UPLOAD_001");
            }
        }
        String newImageUrl = s3Service.uploadFile(storeImage, "store-images");
        store.changeStoreImage(newImageUrl);
        actorRepository.save(store);
        return newImageUrl;
    }


    // 가맹점 사업자등록증 변경
    @Override
    @Transactional
    public ChangeBusinessInfoResponse changeBusinessInfo(String businessNumber, MultipartFile businessImage, String userEmail) {
        log.info("가맹점 사업자등록증 변경 시작- 사용자 이메일: {}", userEmail);
        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        String oldImageUrl = store.getBusinessImage();
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            try {
                s3Service.extractKeyFromUrl(oldImageUrl);
            } catch (Exception e) {
                throw exceptionDbService.getException("S3_UPLOAD_001");
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


    // 결제코인 등록
    @Transactional
    @Override
    public ChangeCoinStatusResponse registerStoreCoin(ChangeCoinStatusRequest request, String userEmail) {
        log.info("가맹점 코인 결제수단 등록 시작 - 사용자 이메일: {}", userEmail);
        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        // 요청된 코인 이름(String)을 Coin 엔티티로 변환
        List<Coin> coins = request.getSupportedCoins().stream()
                .map(coinName -> coinRepository.findByCurrency(coinName))
                .collect(Collectors.toList());

        // Actor 엔티티에 변환된 Coin 리스트 설정
        store.changeSupportedCoins(coins);

        // 응답 생성
        ChangeCoinStatusResponse res = ChangeCoinStatusResponse.builder()
                .supportedCoins(coins)
                .build();

        return res;
    }


    // 영업중 상태 변경
    @Transactional
    @Override
    public ChangeStoreStatusResponse changeStoreStatus(ChangeStoreStatusRequest request, String userEmail) {
        log.info("가맹점 영업중 상태 변경 시작 - 사용자 이메일: {}", userEmail);

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        store.changeStoreStatus(request.getStoreStatus());
        ChangeStoreStatusResponse res = ChangeStoreStatusResponse.builder()
                .storeStatus(store.getStatus())
                .build();
        return res;
    }


    // 내 가게 조회
    @Override
    public GetMyStoreAllDetailResponse getMyStoreAllDetails(String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        List<Menu> menus = menuRepository.findAllByStoreId(store.getId());
        List<GetMenuDetailResponse> menuResponse = menus.stream()
                .map(menu -> GetMenuDetailResponse.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getName())
                        .menuPrice(menu.getPrice())
                        .menuImage(menu.getImage())
                        .build())
                .collect(Collectors.toList());

        boolean isLiked = likeRepository.findByUserAndStore(store, store)
                .map(Like::isActive)
                .orElse(false);

        GetMyStoreAllDetailResponse res =
                GetMyStoreAllDetailResponse.builder().storeId(store.getId()).email(store.getEmail()).likeCount(likeRepository.countByStoreAndActiveTrue(store)).storeName(store.getName())
                .storeAddress(store.getStoreAddress()).storeStatus(store.getStatus()).storeImageUrl(store.getStoreImage())
                .coinList(store.getCoinList()).menuList(menuResponse).isLiked(isLiked).build();

        return res;
    }


    // 영업중인 모든 가게 조회
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


    // 가게 상세 조회
    @Override
    @Transactional(readOnly = true)
    public GetOneStoreDetailResponse getOneStoreDetail(Long storeId) {

        Actor store = actorRepository.findById(storeId)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

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

        boolean isLiked = likeRepository.findByUserAndStore(store, store)
                .map(Like::isActive)
                .orElse(false);

        return GetOneStoreDetailResponse.builder()
                .likeCount(likeCount)
                .storeName(store.getName())
                .storeAddress(store.getStoreAddress())
                .storeImageUrl(store.getStoreImage())
                .coinList(store.getCoinList())
                .menuList(menuResponse)
                .isLiked(isLiked)
                .build();
    }

    @Override
    public Long getStoreIdByEmail(String email) {
        return actorRepository.findByEmail(email)
                .filter(BaseEntity -> BaseEntity instanceof Actor)
                .map(BaseEntity -> ((Actor) BaseEntity).getId())
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));
    }


}