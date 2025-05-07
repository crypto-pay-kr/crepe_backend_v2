//package dev.crepe.domain.channel.actor.store.repository;
//
//import dev.crepe.domain.channel.actor.store.model.StoreStatus;
//import dev.crepe.domain.channel.actor.store.model.entity.Store;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface StoreRepository extends JpaRepository<St, Long> {
//    Optional<Store> findByEmail(String email);
//    boolean existsByEmail(String email);
//    boolean existsByStoreName(String name);
//    boolean existsByPhoneNumber(String phoneNumber);
//    Optional<Store> findOneByEmail(String userEmail);
//    List<Store> findByDataStatusTrueAndStatus(StoreStatus status);
//    Optional<Store> findByIdAndDataStatusTrueAndStatus(Long id, StoreStatus status);
//    boolean existsByIdAndEmail(Long storeId, String userEmail);
//
//}
//
