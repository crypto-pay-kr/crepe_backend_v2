package dev.crepe.domain.channel.actor.store.service;


import dev.crepe.domain.channel.actor.store.model.dto.request.RegisterOrChangeMenuRequest;
import dev.crepe.domain.channel.actor.store.model.dto.response.GetMenuDetailResponse;
import dev.crepe.domain.channel.actor.store.model.dto.response.GetMenuListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface MenuService {
    ResponseEntity<Void> registerStoreMenu(RegisterOrChangeMenuRequest request, MultipartFile menuImage, String userEmail);
    ResponseEntity<Void> changeStoreMenu(RegisterOrChangeMenuRequest request, Long menuId, MultipartFile menuImage, String userEmail);
    ResponseEntity<Void> deleteStoreMenu(Long menuId, String userEmail);
    GetMenuDetailResponse getMenuDetailById(Long menuId, String userEmail);
    GetMenuListResponse getAllMenusByStore(Long storeId, String userEmail);
}
