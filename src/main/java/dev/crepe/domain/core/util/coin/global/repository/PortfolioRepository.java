package dev.crepe.domain.core.util.coin.global.repository;

import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByBankToken(BankToken bankToken);

    void deleteAllByBankToken(BankToken bankToken);
}
