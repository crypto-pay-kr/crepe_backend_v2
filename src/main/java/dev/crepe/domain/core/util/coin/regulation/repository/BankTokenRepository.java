package dev.crepe.domain.core.util.coin.regulation.repository;

import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTokenRepository extends JpaRepository<BankToken, Long> {
}
