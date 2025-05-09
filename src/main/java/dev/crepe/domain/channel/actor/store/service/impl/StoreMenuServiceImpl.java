package dev.crepe.domain.channel.actor.store.service.impl;


import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.store.exception.StoreNotFoundException;
import dev.crepe.domain.channel.actor.store.exception.UnauthorizedStoreAccessException;
import dev.crepe.domain.channel.actor.store.model.dto.request.RegisterOrChangeMenuRequest;
import dev.crepe.domain.channel.actor.store.model.dto.response.GetMenuDetailResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.GetMenuListResponse;

import dev.crepe.domain.channel.actor.store.repository.MenuRepository;

import dev.crepe.domain.channel.actor.store.service.MenuService;
import dev.crepe.domain.channel.market.menu.model.entity.Menu;
import dev.crepe.global.error.exception.ResourceNotFoundException;
import dev.crepe.global.error.exception.UnauthorizedException;
import dev.crepe.infra.s3.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreMenuServiceImpl implements MenuService {
    private final MenuRepository menuRepository;
    private final ActorRepository actorRepository;
    private final S3Service s3Service;

    @Transactional
    public ResponseEntity<Void> registerStoreMenu(RegisterOrChangeMenuRequest request, MultipartFile menuImage, String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedStoreAccessException(userEmail));

        String storeImageUrl = s3Service.uploadFile(menuImage, "menu-images");
        Menu menu = Menu.builder()
                .name(request.getName())
                .price(request.getPrice())
                .image(storeImageUrl)
                .store(store)
                .build();

        menuRepository.save(menu);
        return ResponseEntity.ok(null);
    }

    @Transactional
    @Override
    public ResponseEntity<Void> changeStoreMenu(RegisterOrChangeMenuRequest request, Long menuId, MultipartFile menuImage, String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedStoreAccessException(userEmail));

        Long storeId = store.getId();
        if (!"SELLER".equals(store.getRole())) {
            throw new SecurityException("해당 가게의 메뉴를 변경할 권한이 없습니다.");
        }
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 메뉴입니다. ID: " + menuId));

        if (!menu.getStore().getId().equals(storeId)) {
            throw new UnauthorizedException("해당 가게의 메뉴가 아닙니다.");
        }
        String storeImageUrl = s3Service.uploadFile(menuImage, "menu-images");
        menu.updateMenu(
                request.getName(),
                request.getPrice(),
                storeImageUrl
        );
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<Void> deleteStoreMenu(Long menuId, String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedStoreAccessException(userEmail));

        Long storeId = store.getId();
        Menu menu = menuRepository.findByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다."));

        if (!menu.getStore().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("메뉴를 삭제할 권한이 없습니다.");
        }

        menu.delete();
        menuRepository.save(menu);
        return ResponseEntity.ok(null);
    }

    @Override
    public GetMenuDetailResponse getMenuDetailById(Long menuId, String userEmail) {
        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedStoreAccessException(userEmail));
        Long storeId = store.getId();
        Menu menu = menuRepository.findByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다."));

        GetMenuDetailResponse res = GetMenuDetailResponse.builder().menuId(menu.getId())
                .menuName(menu.getName()).menuPrice(menu.getPrice()).menuImage(menu.getImage()).build();

        return res;
    }

    public GetMenuListResponse getAllMenusByStore(Long storeId, String userEmail) {
        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedStoreAccessException(userEmail));

        if (!store.getEmail().equals(userEmail) || !"SELLER".equals(store.getRole())) {
            throw new AccessDeniedException("해당 가맹점에 접근 권한이 없습니다.");
        }


        List<Menu> menuList = menuRepository.findAllByStoreId(store.getId());

        List<GetMenuDetailResponse> menuDetails = menuList.stream()
                .map(menu -> GetMenuDetailResponse.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getName())
                        .menuPrice(menu.getPrice())
                        .menuImage(menu.getImage())
                        .build())
                .collect(Collectors.toList());

        return GetMenuListResponse.builder()
                .menus(menuDetails)
                .build();
    }


}
