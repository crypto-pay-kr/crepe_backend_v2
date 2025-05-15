package dev.crepe.domain.core.util.coin.regulation.repository;

import dev.crepe.domain.core.util.coin.regulation.model.entity.TokenPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenPriceRepository extends JpaRepository<TokenPrice, Long> {

}
