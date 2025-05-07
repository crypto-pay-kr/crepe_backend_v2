package dev.crepe.domain.channel.actor.store.repository;

import dev.crepe.domain.channel.market.menu.model.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByIdAndStoreId(Long id, Long storeId);


    @Query("SELECT m FROM Menu m WHERE m.store.id = :storeId AND m.dataStatus = true")
    List<Menu> findAllByStoreId(@Param("storeId") Long storeId);
}