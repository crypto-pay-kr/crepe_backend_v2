package dev.crepe.domain.channel.actor.store.service.impl;


import dev.crepe.domain.auth.UserRole;
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
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.error.exception.ResourceNotFoundException;
import dev.crepe.global.error.exception.UnauthorizedException;
import dev.crepe.infra.s3.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreMenuServiceImpl implements MenuService {
    private final MenuRepository menuRepository;
    private final ActorRepository actorRepository;
    private final S3Service s3Service;
    private final ExceptionDbService exceptionDbService;

    @Transactional
    public ResponseEntity<Void> registerStoreMenu(RegisterOrChangeMenuRequest request, MultipartFile menuImage, String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

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
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        Long storeId = store.getId();

        if (store.getRole() != UserRole.SELLER) {
            throw exceptionDbService.getException("ACTOR_001");
        }

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> exceptionDbService.getException("MENU_001"));

        if (!menu.getStore().getId().equals(storeId)) {
            throw exceptionDbService.getException("ACTOR_001");
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
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        Long storeId = store.getId();
        Menu menu = menuRepository.findByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> exceptionDbService.getException("MENU_001"));

        if (!menu.getStore().getEmail().equals(userEmail)) {
            throw exceptionDbService.getException("ACTOR_001");
        }

        menu.delete();
        menuRepository.save(menu);
        return ResponseEntity.ok(null);
    }

    @Override
    public GetMenuDetailResponse getMenuDetailById(Long menuId, String userEmail) {
        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));
        Long storeId = store.getId();
        Menu menu = menuRepository.findByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> exceptionDbService.getException("MENU_001"));

        GetMenuDetailResponse res = GetMenuDetailResponse.builder().menuId(menu.getId())
                .menuName(menu.getName()).menuPrice(menu.getPrice()).menuImage(menu.getImage()).build();

        return res;
    }

    public GetMenuListResponse getAllMenusByStore(Long storeId, String userEmail) {
        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> exceptionDbService.getException("STORE_001"));

        if (!store.getEmail().equals(userEmail) || !"SELLER".equals(store.getRole())) {
            throw exceptionDbService.getException("ACTOR_001"); // 권한이 없는 유저입니다.
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
